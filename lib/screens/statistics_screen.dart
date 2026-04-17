import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/timer_provider.dart';
import '../theme/app_theme.dart';
import '../models/meditation_session.dart';

class StatisticsScreen extends StatelessWidget {
  const StatisticsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<TimerProvider>(
      builder: (context, provider, child) {
        return StatisticsContent(
          totalMinutes: provider.totalMinutes,
          history: provider.sessionHistory,
        );
      },
    );
  }
}

class StatisticsContent extends StatelessWidget {
  final int totalMinutes;
  final List<MeditationSession> history;

  const StatisticsContent({
    super.key,
    required this.totalMinutes,
    required this.history,
  });

  @override
  Widget build(BuildContext context) {
    // Calculate total days (unique days in history)
    String dateFormat(DateTime date) =>
        '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
    final totalDays = history
        .map((s) => dateFormat(DateTime.fromMillisecondsSinceEpoch(s.date)))
        .toSet()
        .length;

    return Scaffold(
      backgroundColor: AppTheme.black,
      body: Container(
        padding: const EdgeInsets.all(32),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Header
              Text(
                'STATS',
                style: AppTheme.notoSansMedium.copyWith(
                  fontSize: 16,
                  color: AppTheme.gray,
                ),
              ),
              const SizedBox(height: 24),

              if (history.isEmpty) ...[
                Text(
                  'no meditation yet',
                  style: AppTheme.notoSansRegular.copyWith(
                    fontSize: 16,
                    color: AppTheme.darkGray,
                  ),
                ),
              ] else ...[
                // Stats Row
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Column(
                      children: [
                        Text(
                          '$totalDays',
                          style: AppTheme.notoSansLight.copyWith(
                            fontSize: 45,
                            color: AppTheme.white,
                          ),
                        ),
                        Text(
                          'Days',
                          style: AppTheme.notoSansRegular.copyWith(
                            fontSize: 16,
                            color: AppTheme.gray,
                          ),
                        ),
                      ],
                    ),
                    Column(
                      children: [
                        Text(
                          '$totalMinutes',
                          style: AppTheme.notoSansLight.copyWith(
                            fontSize: 45,
                            color: AppTheme.white,
                          ),
                        ),
                        Text(
                          'Minutes',
                          style: AppTheme.notoSansRegular.copyWith(
                            fontSize: 16,
                            color: AppTheme.gray,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),

                const SizedBox(height: 64),

                // Cumulative Line Graph
                SizedBox(
                  height: 150,
                  width: double.infinity,
                  child: CustomPaint(
                    painter: CumulativeGraphPainter(history: history),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class CumulativeGraphPainter extends CustomPainter {
  final List<MeditationSession> history;

  CumulativeGraphPainter({required this.history});

  @override
  void paint(Canvas canvas, Size size) {
    if (history.isEmpty) return;

    final sortedHistory = [...history]..sort((a, b) => a.date.compareTo(b.date));
    final dataPoints = <double>[];
    var cumulativeSeconds = 0;

    for (final session in sortedHistory) {
      cumulativeSeconds += session.duration;
      dataPoints.add(cumulativeSeconds / 60.0); // Convert to minutes
    }

    if (dataPoints.isEmpty) return;

    final paint = Paint()
      ..color = AppTheme.white
      ..strokeWidth = 2
      ..style = PaintingStyle.stroke;

    final width = size.width;
    final height = size.height;
    final maxMinutes = dataPoints.last.clamp(1.0, double.infinity);
    final xStep = dataPoints.length > 1 ? width / (dataPoints.length - 1) : width;

    final path = Path();

    for (var i = 0; i < dataPoints.length; i++) {
      final x = i * xStep;
      final y = height - (dataPoints[i] / maxMinutes * height);

      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant CumulativeGraphPainter oldDelegate) =>
      oldDelegate.history.length != history.length;
}
