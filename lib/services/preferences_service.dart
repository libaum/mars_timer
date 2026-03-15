import 'package:shared_preferences/shared_preferences.dart';

class PreferencesService {
  static const String _lastDurationKey = 'last_duration';

  final SharedPreferences _prefs;

  PreferencesService(this._prefs);

  static Future<PreferencesService> create() async {
    final prefs = await SharedPreferences.getInstance();
    return PreferencesService(prefs);
  }

  int getLastDuration() {
    return _prefs.getInt(_lastDurationKey) ?? 20;
  }

  Future<void> saveLastDuration(int duration) async {
    await _prefs.setInt(_lastDurationKey, duration);
  }
}

