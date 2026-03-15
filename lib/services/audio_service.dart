import 'package:audioplayers/audioplayers.dart';

class AudioService {
  static final AudioService _instance = AudioService._internal();
  factory AudioService() => _instance;
  AudioService._internal();

  final AudioPlayer _player = AudioPlayer();
  bool _isInitialized = false;

  Future<void> initialize() async {
    if (_isInitialized) return;
    await _player.setSource(AssetSource('audio/singing_bowl.mp3'));
    _isInitialized = true;
  }

  Future<void> playSound() async {
    try {
      await _player.stop();
      await _player.seek(Duration.zero);
      await _player.play(AssetSource('audio/singing_bowl.mp3'));
    } catch (e) {
      // Ignore audio errors
    }
  }

  Future<void> stopSound() async {
    try {
      await _player.stop();
    } catch (e) {
      // Ignore audio errors
    }
  }

  void dispose() {
    _player.dispose();
  }
}


