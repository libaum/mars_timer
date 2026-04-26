import 'package:flutter/foundation.dart';
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
        return Stack(
          children: [
            StatisticsContent(
              totalMinutes: provider.totalMinutes,
              history: provider.sessionHistory,
              streak: provider.currentStreak,
              avgMinutes: provider.averageSessionMinutes,
            ),
            if (kDebugMode)
              Positioned(
                left: 0,
                right: 0,
                bottom: 24,
                child: SafeArea(
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      GestureDetector(
                        onTap: provider.seedTestData,
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 8),
                          child: Text(
                            'seed',
                            style: AppTheme.notoSansLight.copyWith(
                              fontSize: 12,
                              color: AppTheme.darkGray,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 24),
                      GestureDetector(
                        onTap: provider.clearAllSessions,
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 8),
                          child: Text(
                            'clear',
                            style: AppTheme.notoSansLight.copyWith(
                              fontSize: 12,
                              color: AppTheme.darkGray,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}

class StatisticsContent extends StatelessWidget {
  final int totalMinutes;
  final List<MeditationSession> history;
  final int streak;
  final double avgMinutes;

  const StatisticsContent({
    super.key,
    required this.totalMinutes,
    required this.history,
    required this.streak,
    required this.avgMinutes,
  });

  @override
  Widget build(BuildContext context) {
    if (history.isEmpty) {
      return Scaffold(
        backgroundColor: AppTheme.black,
        body: SafeArea(
          child: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'no sessions yet',
                  style: AppTheme.notoSansLight.copyWith(
                    fontSize: 20,
                    color: AppTheme.gray,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'start your first meditation',
                  style: AppTheme.notoSansLight.copyWith(
                    fontSize: 13,
                    color: AppTheme.darkGray,
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    }

    final totalDays = history
        .map((s) {
          final d = DateTime.fromMillisecondsSinceEpoch(s.date);
          return '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
        })
        .toSet()
        .length;

    return Scaffold(
      backgroundColor: AppTheme.black,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'STATS',
                style: AppTheme.notoSansMedium.copyWith(
                  fontSize: 13,
                  letterSpacing: 2,
                  color: AppTheme.gray,
                ),
              ),
              const SizedBox(height: 48),

              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _StatCell(
                    value: streak > 0 ? '$streak' : '—',
                    label: 'day streak',
                  ),
                  _StatCell(value: '$totalDays', label: 'days'),
                  _StatCell(value: '$totalMinutes', label: 'minutes'),
                ],
              ),

              const SizedBox(height: 16),

              Text(
                'avg ${avgMinutes.round()} min / session',
                style: AppTheme.notoSansLight.copyWith(
                  fontSize: 13,
                  color: AppTheme.gray,
                ),
              ),

              const SizedBox(height: 56),

              SizedBox(
                height: 120,
                width: double.infinity,
                child: CustomPaint(
                  painter: DailyBarChartPainter(history: history),
                ),
              ),

              const SizedBox(height: 8),

              Text(
                'last 30 days',
                style: AppTheme.notoSansLight.copyWith(
                  fontSize: 11,
                  letterSpacing: 1,
                  color: AppTheme.darkGray,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatCell extends StatelessWidget {
  final String value;
  final String label;

  const _StatCell({required this.value, required this.label});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: AppTheme.notoSansThin.copyWith(
            fontSize: 48,
            color: AppTheme.white,
            fontFeatures: const [FontFeature.tabularFigures()],
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: AppTheme.notoSansLight.copyWith(
            fontSize: 12,
            letterSpacing: 1,
            color: AppTheme.gray,
          ),
        ),
      ],
    );
  }
}

class DailyBarChartPainter extends CustomPainter {
  final List<MeditationSession> history;

  DailyBarChartPainter({required this.history});

  @override
  void paint(Canvas canvas, Size size) {
    if (history.isEmpty) return;

    const days = 30;
    final now = DateTime.now();

    String dateStr(DateTime d) =>
        '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

    final dailyMinutes = <String, double>{};
    for (final session in history) {
      final key = dateStr(DateTime.fromMillisecondsSinceEpoch(session.date));
      dailyMinutes[key] = (dailyMinutes[key] ?? 0) + session.duration / 60.0;
    }

    final values = <double>[
      for (int i = days - 1; i >= 0; i--)
        dailyMinutes[dateStr(now.subtract(Duration(days: i)))] ?? 0,
    ];

    final maxVal = values.fold(0.0, (a, b) => a > b ? a : b).clamp(1.0, double.infinity);
    final barWidth = size.width / days;
    final gap = barWidth * 0.3;

    final activePaint = Paint()
      ..color = AppTheme.white
      ..style = PaintingStyle.fill;

    final emptyPaint = Paint()
      ..color = AppTheme.darkGray
      ..style = PaintingStyle.fill;

    for (int i = 0; i < days; i++) {
      final x = i * barWidth + gap / 2;
      final w = barWidth - gap;

      if (values[i] > 0) {
        final h = (values[i] / maxVal * size.height).clamp(3.0, size.height);
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(x, size.height - h, w, h),
            const Radius.circular(2),
          ),
          activePaint,
        );
      } else {
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(x, size.height - 2, w, 2),
            const Radius.circular(1),
          ),
          emptyPaint,
        );
      }
    }
  }

  @override
  bool shouldRepaint(covariant DailyBarChartPainter oldDelegate) =>
      oldDelegate.history.length != history.length;
}
