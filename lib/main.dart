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

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (Platform.isWindows || Platform.isLinux) {
    // Initialize FFI
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  }

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

  // Enable edge-to-edge
  SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);

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
