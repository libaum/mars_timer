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
  int _remainingTime = 20 * 60 * 1000; // in milliseconds
  bool _wasPausedDuringPrep = false;
  bool _isTestDuration = false;

  Timer? _timer;
  int _targetTime = 0;
  TimerState _previousState = TimerState.idle;

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
  bool get wasPausedDuringPrep => _wasPausedDuringPrep;
  int get totalMinutes => _totalMinutes;
  List<MeditationSession> get sessionHistory => _sessionHistory;

  bool get isIdle => _timerState == TimerState.idle || _timerState == TimerState.finished;
  bool get isRunning => _timerState == TimerState.running || _timerState == TimerState.prep;
  bool get isPaused => _timerState == TimerState.paused;
  bool get isWarmup => _timerState == TimerState.prep;

  Future<void> _initialize() async {
    // Load saved duration
    final lastDuration = _preferencesService.getLastDuration();
    _meditationTime = lastDuration > 0 ? lastDuration : 20;
    _remainingTime = _meditationTime * 60 * 1000;

    // Load statistics
    await _loadStatistics();

    // Initialize audio
    await _audioService.initialize();

    notifyListeners();
  }

  Future<void> _loadStatistics() async {
    final totalSeconds = await _databaseService.getTotalMeditationTime();
    _totalMinutes = totalSeconds ~/ 60;
    _sessionHistory = await _databaseService.getAllSessions();
    notifyListeners();
  }

  void startTimer() async {
    await WakelockPlus.enable();

    _wasPausedDuringPrep = false;
    final totalMeditationMs = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;

    if (_prepTime > 0) {
      // Start with prep time
      _timerState = TimerState.prep;
      _remainingTime = _prepTime * 1000;
      _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
      _startCountdown(() {
        // Prep done, play sound and start meditation
        _audioService.playSound();
        _timerState = TimerState.running;
        _remainingTime = totalMeditationMs;
        _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
        _wasPausedDuringPrep = false;
        notifyListeners();
        _startCountdown(_onMeditationComplete);
      });
    } else {
      // Start meditation directly
      _audioService.playSound();
      _timerState = TimerState.running;
      _remainingTime = totalMeditationMs;
      _targetTime = DateTime.now().millisecondsSinceEpoch + _remainingTime;
      notifyListeners();
      _startCountdown(_onMeditationComplete);
    }

    notifyListeners();
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
      }

      notifyListeners();
    });
  }

  void _onMeditationComplete() async {
    _audioService.playSound();
    await _saveSession();
    _timerState = TimerState.finished;
    _remainingTime = _meditationTime * 60 * 1000;
    await WakelockPlus.disable();
    notifyListeners();
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

  void stopTimer() async {
    _timer?.cancel();
    _audioService.stopSound();
    _timerState = TimerState.idle;
    // Keep test mode active until user changes duration manually
    _remainingTime = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
    _wasPausedDuringPrep = false;
    await WakelockPlus.disable();
    notifyListeners();
  }

  Future<void> savePartialSession() async {
    final totalMeditationMs = _isTestDuration ? 5000 : _meditationTime * 60 * 1000;
    final elapsedMs = totalMeditationMs - _remainingTime;
    final elapsedSeconds = elapsedMs ~/ 1000;

    if (elapsedSeconds > 0 && !_wasPausedDuringPrep) {
      final session = MeditationSession(
        date: DateTime.now().millisecondsSinceEpoch,
        duration: elapsedSeconds,
      );
      await _databaseService.insertSession(session);
      await _loadStatistics();
    }

    stopTimer();
  }

  Future<void> _saveSession() async {
    final durationSeconds = _isTestDuration ? 5 : _meditationTime * 60;
    final session = MeditationSession(
      date: DateTime.now().millisecondsSinceEpoch,
      duration: durationSeconds,
    );
    await _databaseService.insertSession(session);
    await _loadStatistics();
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
    notifyListeners();
  }

  void decrementPrepTime() {
    _prepTime = (_prepTime - 5).clamp(0, _prepTime);
    notifyListeners();
  }

  void setTestDuration() {
    _isTestDuration = true;
    _meditationTime = 1; // display as 1 min usually but we override remaining
    _remainingTime = 5 * 1000; // 5 seconds
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
