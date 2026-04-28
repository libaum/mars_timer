import 'dart:io';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'providers/timer_provider.dart';
import 'services/database_service.dart';
import 'services/preferences_service.dart';
import 'services/audio_service.dart';
import 'theme/app_theme.dart';
import 'screens/timer_screen.dart';
import 'services/foreground_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize database factory for all platforms
  if (Platform.isWindows || Platform.isLinux) {
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  } else if (Platform.isAndroid || Platform.isIOS) {
    // For Android and iOS, sqflite handles it automatically
    // but we make sure it's available
  }

  ForegroundService.initialize();

  // Initialize services
  final preferencesService = await PreferencesService.create();
  final databaseService = DatabaseService();
  final audioService = AudioService();

  // Set system UI overlay style
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.light,
    systemNavigationBarColor: Colors.transparent,
    systemNavigationBarIconBrightness: Brightness.light,
  ));

  // Hide status bar, keep navigation bar
  SystemChrome.setEnabledSystemUIMode(
    SystemUiMode.manual,
    overlays: [SystemUiOverlay.bottom],
  );

  runApp(
    MarsTimerApp(
      preferencesService: preferencesService,
      databaseService: databaseService,
      audioService: audioService,
    ),
  );
}

class MarsTimerApp extends StatelessWidget {
  final PreferencesService preferencesService;
  final DatabaseService databaseService;
  final AudioService audioService;

  const MarsTimerApp({
    super.key,
    required this.preferencesService,
    required this.databaseService,
    required this.audioService,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => TimerProvider(
        databaseService: databaseService,
        preferencesService: preferencesService,
        audioService: audioService,
      ),
      child: MaterialApp(
        title: 'Mars Timer',
        theme: AppTheme.darkTheme,
        debugShowCheckedModeBanner: false,
        home: const TimerScreen(),
      ),
    );
  }
}
