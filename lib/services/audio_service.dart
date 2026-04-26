import 'dart:async';
import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/foundation.dart';

class AudioService {
  static final AudioService _instance = AudioService._internal();
  factory AudioService() => _instance;
  AudioService._internal();

  final AudioPlayer _player = AudioPlayer();
  Timer? _fadeTimer;
  bool _isInitialized = false;

  Future<void> initialize() async {
    if (_isInitialized) return;
    await AudioPlayer.global.setAudioContext(AudioContext(
      android: const AudioContextAndroid(
        contentType: AndroidContentType.music,
        usageType: AndroidUsageType.media,
        audioFocus: AndroidAudioFocus.gain,
      ),
      iOS: AudioContextIOS(
        category: AVAudioSessionCategory.playback,
      ),
    ));
    await _player.setReleaseMode(ReleaseMode.stop);
    _isInitialized = true;
  }

  Future<void> playSound() async {
    _fadeTimer?.cancel();
    try {
      await _player.stop();
      await _player.setVolume(1.0);
      // Don't await play(): on Android, audioplayers' play future can hang
      // for 30s when called repeatedly. Fire-and-forget instead.
      unawaited(_player.play(AssetSource('audio/singing_bowl.mp3')));
      _startFadeOut();
    } catch (e) {
      debugPrint('AudioService.playSound error: $e');
    }
  }

  void _startFadeOut({int durationSeconds = 10}) {
    const steps = 40;
    final interval = Duration(
      milliseconds: durationSeconds * 1000 ~/ steps,
    );
    var step = 0;

    _fadeTimer = Timer.periodic(interval, (timer) async {
      step++;
      final volume = 1.0 - step / steps;
      if (volume <= 0) {
        timer.cancel();
        try {
          await _player.stop();
          await _player.setVolume(1.0);
        } catch (_) {}
      } else {
        try {
          await _player.setVolume(volume);
        } catch (_) {}
      }
    });
  }

  Future<void> stopSound() async {
    _fadeTimer?.cancel();
    try {
      await _player.setVolume(1.0);
      await _player.stop();
    } catch (e) {
      // Ignore audio errors
    }
  }

  void dispose() {
    _fadeTimer?.cancel();
    _player.dispose();
  }
}
