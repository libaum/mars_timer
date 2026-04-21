import 'dart:io';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';

class ForegroundService {
  static bool get _supported => Platform.isAndroid;

  static void initialize() {
    if (!_supported) return;
    FlutterForegroundTask.init(
      androidNotificationOptions: AndroidNotificationOptions(
        channelId: 'mars_timer_channel',
        channelName: 'Mars Timer',
        channelImportance: NotificationChannelImportance.LOW,
        priority: NotificationPriority.LOW,
      ),
      iosNotificationOptions: const IOSNotificationOptions(
        showNotification: false,
      ),
      foregroundTaskOptions: ForegroundTaskOptions(
        eventAction: ForegroundTaskEventAction.nothing(),
        autoRunOnBoot: false,
        allowWakeLock: true,
      ),
    );
  }

  static Future<void> start(String text) async {
    if (!_supported) return;
    await FlutterForegroundTask.startService(
      serviceId: 256,
      notificationTitle: 'Mars Timer',
      notificationText: text,
    );
  }

  static Future<void> update(String text) async {
    if (!_supported) return;
    try {
      await FlutterForegroundTask.updateService(
        notificationTitle: 'Mars Timer',
        notificationText: text,
      );
    } catch (_) {}
  }

  static Future<void> stop() async {
    if (!_supported) return;
    try {
      await FlutterForegroundTask.stopService();
    } catch (_) {}
  }
}
