import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/meditation_session.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  static Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'mars_timer_database.db');

    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE meditation_sessions(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date INTEGER NOT NULL,
            duration INTEGER NOT NULL
          )
        ''');
      },
    );
  }

  Future<int> insertSession(MeditationSession session) async {
    final db = await database;
    return await db.insert('meditation_sessions', {
      'date': session.date,
      'duration': session.duration,
    });
  }

  Future<List<MeditationSession>> getAllSessions() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'meditation_sessions',
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => MeditationSession.fromMap(maps[i]));
  }

  Future<int> getTotalMeditationTime() async {
    final db = await database;
    final result = await db.rawQuery('SELECT SUM(duration) as total FROM meditation_sessions');
    final total = result.first['total'];
    return total != null ? total as int : 0;
  }

  Future<int> getSessionsCount() async {
    final db = await database;
    final result = await db.rawQuery('SELECT COUNT(*) as count FROM meditation_sessions');
    return result.first['count'] as int;
  }

  Future<void> clearAllSessions() async {
    final db = await database;
    await db.delete('meditation_sessions');
  }
}

