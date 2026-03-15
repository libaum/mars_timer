/// Represents a completed meditation session
class MeditationSession {
  final int? id;
  final int date; // Timestamp in milliseconds
  final int duration; // Duration in seconds

  MeditationSession({
    this.id,
    required this.date,
    required this.duration,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'date': date,
      'duration': duration,
    };
  }

  factory MeditationSession.fromMap(Map<String, dynamic> map) {
    return MeditationSession(
      id: map['id'] as int?,
      date: map['date'] as int,
      duration: map['duration'] as int,
    );
  }

  @override
  String toString() {
    return 'MeditationSession{id: $id, date: $date, duration: $duration}';
  }
}

