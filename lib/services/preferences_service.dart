import 'package:shared_preferences/shared_preferences.dart';

class PreferencesService {
  static const String _lastDurationKey = 'last_duration';
  static const String _prepTimeKey = 'prep_time';
  static const String _quickSelectSlotsKey = 'quick_select_slots';

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

  int getLastPrepTime() {
    return _prefs.getInt(_prepTimeKey) ?? 0;
  }

  Future<void> saveLastPrepTime(int seconds) async {
    await _prefs.setInt(_prepTimeKey, seconds);
  }

  List<int> getQuickSelectSlots() {
    final stored = _prefs.getStringList(_quickSelectSlotsKey);
    if (stored == null || stored.length != 4) return [5, 10, 15, 20];
    return stored.map((s) => int.tryParse(s) ?? 5).toList();
  }

  Future<void> saveQuickSelectSlots(List<int> slots) async {
    await _prefs.setStringList(
      _quickSelectSlotsKey,
      slots.map((s) => s.toString()).toList(),
    );
  }
}
