import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../models/meditation_session.dart';
import '../services/database_service.dart';
import '../services/preferences_service.dart';
import '../services/audio_service.dart';

enum TimerState {
  idle,
  prep,
  running,
  paused,
  finished,
}

class TimerProvider extends ChangeNotifier {
  final DatabaseService _databaseService;
  final PreferencesService _preferencesService;
  final AudioService _audioService;

  TimerState _timerState = TimerState.idle;
  int _meditationTime = 20; // in minutes
  int _prepTime = 0; // in seconds
  List<int> _quickSelectSlots = [5, 10, 15, 20];
  int _remainingTime = 20 * 60 * 1000; // in milliseconds
  bool _wasPausedDuringPrep = false;
  bool _isTestDuration = false;

  Timer? _timer;
  int _targetTime = 0;
  TimerState _previousState = TimerState.idle;

  // Overtime (finished state)
  Timer? _overtimeTimer;
  int _overtimeMs = 0;
  bool _overtimeAccepted = false;

  // Statistics
  int _totalMinutes = 0;
  List<MeditationSession> _sessionHistory = [];

  TimerProvider({
    required DatabaseService databaseService,
    required PreferencesService preferencesService,
    required AudioService audioService,
  })  : _databaseService = databaseService,
        _preferencesService = preferencesService,
        _audioService = audioService {
    _initialize();
  }

  // Getters
  TimerState get timerState => _timerState;
  int get meditationTime => _meditationTime;
  int get prepTime => _prepTime;
  int get remainingTime => _remainingTime;
  int get overtimeMs => _overtimeMs;
  bool get overtimeAccepted => _overtimeAccepted;
  bool get wasPausedDuringPrep => _wasPausedDuringPrep;
  int get totalMinutes => _totalMinutes;
  List<MeditationSession> get sessionHistory => _sessionHistory;

  List<int> get quickSelectSlots => _quickSelectSlots;

  bool get isIdle => _timerState == TimerState.idle;
  bool get isFinished => _timerState == TimerState.finished;
  bool get isRunning => _timerState == TimerState.running || _timerState == TimerState.prep;
  bool get isPaused => _timerState == TimerState.paused;
  bool get isWarmup => _timerState == TimerState.prep;
  int get totalMeditationMs => _isTestDuration ? 5000 : _meditationTime * 60 * 1000;

  Future<void> _initialize() async {
    try {
      final lastDuration = _preferencesService.getLastDuration();
      _meditationTime = lastDuration > 0 ? lastDuration : 20;
      _remainingTime = _meditationTime * 60 * 1000;
      _prepTime = _preferencesService.getLastPrepTime();
      _quickSelectSlots = _preferencesService.getQuickSelectSlots();

      await _loadStatistics();
      await _audioService.initialize();

      notifyListeners();
    } catch (e) {
      debugPrint('Error during initialization: $e');
      notifyListeners();
    }
  }

  Future<void> _loadStatistics() async {
    try {
      final totalSeconds = await _databaseService.getTotalMeditationTime();
      _totalMinutes = totalSeconds ~/ 60;
      _sessionHistory = await _databaseService.getAllSessions();
      notifyListeners();
    } catch (e) {
      debugPrint('Error loading statistics: $e');
      _totalMinutes = 0;
      _sessionHistory = [];
    }
  }

  Future<void> startTimer() async {
    await WakelockPlus.enable();

    _wasPausedDuringPrep = false;
    final totalMeditationMs = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;

    if (_prepTime > 0) {
      _timerState = TimerState.prep;
      _remainingTime = _prepTime * 1000;
      _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
      notifyListeners();
      _startCountdown(() {
        _audioService.playSound();
        _timerState = TimerState.running;
        _remainingTime = totalMeditationMs;
        _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
        _wasPausedDuringPrep = false;
        notifyListeners();
        _startCountdown(_onMeditationComplete);
      });
    } else {
      _audioService.playSound();
      _timerState = TimerState.running;
      _remainingTime = totalMeditationMs;
      _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
      notifyListeners();
      _startCountdown(_onMeditationComplete);
    }
  }

  void _startCountdown(VoidCallback onComplete) {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(milliseconds: 100), (timer) {
      if (_timerState == TimerState.paused) return;

      final now = DateTime.now().millisecondsSinceEpoch;
      _remainingTime = _targetTime - now;

      if (_remainingTime <= 0) {
        _remainingTime = 0;
        timer.cancel();
        onComplete();
        return;
      }

      notifyListeners();
    });
  }

  // All state changes happen synchronously before any async work,
  // so the UI transitions immediately with no race window.
  void _onMeditationComplete() {
    _audioService.playSound();
    _timerState = TimerState.finished;
    _remainingTime = 0;
    _overtimeMs = 0;
    _overtimeAccepted = false;
    notifyListeners();
    _startOvertimeCounter();
    // Wakelock stays on — user may still be meditating during overtime
  }

  void _startOvertimeCounter() {
    final startTime = DateTime.now().millisecondsSinceEpoch;
    _overtimeTimer = Timer.periodic(const Duration(milliseconds: 100), (timer) {
      _overtimeMs = DateTime.now().millisecondsSinceEpoch - startTime;
      notifyListeners();
    });
  }

  /// Stops the overtime counter and marks the elapsed time for inclusion in save.
  void acceptOvertime() {
    if (_overtimeAccepted) return;
    _overtimeTimer?.cancel();
    _overtimeAccepted = true;
    notifyListeners();
  }

  /// Saves the completed session (original duration + overtime if accepted).
  Future<void> saveFinishedSession() async {
    _overtimeTimer?.cancel();
    final baseDuration = _isTestDuration ? 5 : _meditationTime * 60;
    final overtimeDuration = _overtimeAccepted ? _overtimeMs ~/ 1000 : 0;
    final session = MeditationSession(
      date: DateTime.now().millisecondsSinceEpoch,
      duration: baseDuration + overtimeDuration,
    );
    await _databaseService.insertSession(session);
    await _loadStatistics();
    _resetFinished();
    await WakelockPlus.disable();
    notifyListeners();
  }

  /// Discards the finished session without saving anything.
  Future<void> discardFinishedSession() async {
    _overtimeTimer?.cancel();
    _resetFinished();
    await WakelockPlus.disable();
    notifyListeners();
  }

  void _resetFinished() {
    _timerState = TimerState.idle;
    _remainingTime = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
    _wasPausedDuringPrep = false;
    _overtimeMs = 0;
    _overtimeAccepted = false;
  }

  void pauseTimer() {
    if (_timerState == TimerState.running || _timerState == TimerState.prep) {
      _previousState = _timerState;
      if (_timerState == TimerState.prep) {
        _wasPausedDuringPrep = true;
      }
      _timerState = TimerState.paused;
      _timer?.cancel();
      notifyListeners();
    }
  }

  void resumeTimer() {
    if (_timerState == TimerState.paused) {
      _timerState = _previousState;
      _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;

      if (_previousState == TimerState.prep) {
        _startCountdown(() {
          _audioService.playSound();
          _timerState = TimerState.running;
          _remainingTime = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
          _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
          _wasPausedDuringPrep = false;
          notifyListeners();
          _startCountdown(_onMeditationComplete);
        });
      } else {
        _startCountdown(_onMeditationComplete);
      }

      notifyListeners();
    }
  }

  Future<void> stopTimer() async {
    _timer?.cancel();
    _overtimeTimer?.cancel();
    _audioService.stopSound();
    _timerState = TimerState.idle;
    _remainingTime = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
    _wasPausedDuringPrep = false;
    _overtimeMs = 0;
    _overtimeAccepted = false;
    await WakelockPlus.disable();
    notifyListeners();
  }

  Future<void> savePartialSession() async {
    final totalMs = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
    final elapsedMs = totalMs - _remainingTime;
    final elapsedSeconds = elapsedMs ~/ 1000;

    if (elapsedSeconds > 0 && !_wasPausedDuringPrep) {
      final session = MeditationSession(
        date: DateTime.now().millisecondsSinceEpoch,
        duration: elapsedSeconds,
      );
      await _databaseService.insertSession(session);
      await _loadStatistics();
    }

    await stopTimer();
  }

  void setMeditationTime(int minutes) {
    _isTestDuration = false;
    _meditationTime = minutes.clamp(1, 180);
    _remainingTime = _meditationTime * 60 * 1000;
    _preferencesService.saveLastDuration(_meditationTime);
    notifyListeners();
  }

  void incrementMeditationTime() {
    setMeditationTime(_meditationTime + 1);
  }

  void decrementMeditationTime() {
    setMeditationTime(_meditationTime - 1);
  }

  void incrementPrepTime() {
    _prepTime += 5;
    _preferencesService.saveLastPrepTime(_prepTime);
    notifyListeners();
  }

  void decrementPrepTime() {
    _prepTime = (_prepTime - 5).clamp(0, _prepTime);
    _preferencesService.saveLastPrepTime(_prepTime);
    notifyListeners();
  }

  void setQuickSelectSlot(int index, int minutes) {
    _quickSelectSlots[index] = minutes.clamp(1, 180);
    _preferencesService.saveQuickSelectSlots(_quickSelectSlots);
    notifyListeners();
  }

  void setTestDuration() {
    _isTestDuration = true;
    _meditationTime = 1;
    _remainingTime = 5 * 1000;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    _overtimeTimer?.cancel();
    super.dispose();
  }
}
