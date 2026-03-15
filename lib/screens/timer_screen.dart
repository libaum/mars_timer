import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/timer_provider.dart';
import '../theme/app_theme.dart';
import 'statistics_screen.dart';

class TimerScreen extends StatefulWidget {
  const TimerScreen({super.key});

  @override
  State<TimerScreen> createState() => _TimerScreenState();
}

class _TimerScreenState extends State<TimerScreen> {
  bool _showStats = false;

  @override
  Widget build(BuildContext context) {
    return Consumer<TimerProvider>(
      builder: (context, provider, child) {
        if (_showStats) {
          return PopScope(
            canPop: false,
            onPopInvokedWithResult: (didPop, result) {
              if (!didPop) {
                setState(() => _showStats = false);
              }
            },
            child: GestureDetector(
              onTap: () => setState(() => _showStats = false),
              onLongPress: () => setState(() => _showStats = false),
              child: const StatisticsScreen(),
            ),
          );
        }

        return TimerScreenContent(
          provider: provider,
          onShowStats: () => setState(() => _showStats = true),
        );
      },
    );
  }
}

class TimerScreenContent extends StatelessWidget {
  final TimerProvider provider;
  final VoidCallback onShowStats;

  const TimerScreenContent({
    super.key,
    required this.provider,
    required this.onShowStats,
  });

  @override
  Widget build(BuildContext context) {
    final mediaQuery = MediaQuery.of(context);
    final screenHeight = mediaQuery.size.height;
    final statusBarHeight = mediaQuery.padding.top;

    return Scaffold(
      backgroundColor: AppTheme.black,
      body: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: () {
          debugPrint('TimerScreen: Tap detected');
          if (provider.isIdle) {
            provider.startTimer();
          } else if (provider.isRunning) {
            provider.pauseTimer();
          } else if (provider.isPaused) {
            provider.resumeTimer();
          }
        },
        onLongPress: () {
          if (provider.isIdle) {
            onShowStats();
          }
        },
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.only(top: 24),
            child: Stack(
              fit: StackFit.expand,
              children: [
                // Ensure clicks are captured
                Container(color: Colors.transparent),

                // Timer Display with +/- controls
                Positioned(
                  left: 0,
                  right: 0,
                  top: screenHeight * 0.35 - statusBarHeight,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      // Minus button
                      GestureDetector(
                        onTap: provider.isIdle
                            ? provider.decrementMeditationTime
                            : null,
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Text(
                            '-',
                            style: AppTheme.notoSansThin.copyWith(
                              fontSize: 50,
                              color: AppTheme.white.withValues(
                                alpha: provider.isIdle ? 0.5 : 0,
                              ),
                            ),
                          ),
                        ),
                      ),

                      // Timer
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        child: Text(
                          _formatTime(provider.remainingTime),
                          style: provider.isWarmup
                              ? AppTheme.notoSansThin.copyWith(
                                  fontSize: 72,
                                  color: AppTheme.gray,
                                  fontFeatures: const [
                                    FontFeature.tabularFigures(),
                                  ],
                                )
                              : provider.isPaused
                                  ? AppTheme.notoSansThickerThin.copyWith(
                                      fontSize: 72,
                                      color: AppTheme.gray,
                                      fontFeatures: const [
                                        FontFeature.tabularFigures(),
                                      ],
                                    )
                                  : AppTheme.notoSansThickerThin.copyWith(
                                      fontSize: 72,
                                      color: AppTheme.white,
                                      fontFeatures: const [
                                        FontFeature.tabularFigures(),
                                      ],
                                    ),
                        ),
                      ),

                      // Plus button
                      GestureDetector(
                        onTap: provider.isIdle
                            ? provider.incrementMeditationTime
                            : null,
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Text(
                            '+',
                            style: AppTheme.notoSansThin.copyWith(
                              fontSize: 50,
                              color: AppTheme.white.withValues(
                                alpha: provider.isIdle ? 0.5 : 0,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),

                // Quick Select Buttons (only in Idle)
                if (provider.isIdle)
                  Positioned(
                    left: 0,
                    right: 0,
                    top: screenHeight * 0.52 - statusBarHeight,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        GestureDetector(
                          onTap: provider.setTestDuration,
                          child: Container(
                            width: 56,
                            height: 56,
                            margin: const EdgeInsets.symmetric(horizontal: 8),
                            decoration: BoxDecoration(
                              color: AppTheme.darkGray,
                              borderRadius: BorderRadius.circular(28),
                            ),
                            alignment: Alignment.center,
                            child: Text(
                              '5s',
                              style: AppTheme.notoSansLight.copyWith(
                                fontSize: 16,
                                fontFeatures: const [
                                  FontFeature.tabularFigures(),
                                ],
                                color: AppTheme.white,
                              ),
                            ),
                          ),
                        ),
                        ...[5, 10, 15, 20].map((time) {
                          final isSelected = provider.meditationTime == time;
                          return GestureDetector(
                            onTap: () => provider.setMeditationTime(time),
                            child: Container(
                              width: 56,
                              height: 56,
                              margin: const EdgeInsets.symmetric(horizontal: 8),
                              decoration: BoxDecoration(
                                color: AppTheme.black,
                                borderRadius: BorderRadius.circular(28),
                              ),
                              alignment: Alignment.center,
                              child: Text(
                                '$time',
                                style: AppTheme.notoSansLight.copyWith(
                                  fontSize: 16,
                                  fontFeatures: const [
                                    FontFeature.tabularFigures(),
                                  ],
                                  color: isSelected
                                      ? AppTheme.white
                                      : AppTheme.white.withValues(alpha: 0.7),
                                ),
                              ),
                            ),
                          );
                        }).toList(),
                      ],
                    ),
                  ),

                // Prep Time Controls (only in Idle)
                if (provider.isIdle)
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 72,
                    child: Column(
                      children: [
                        if (provider.prepTime > 0) ...[
                          // Active prep time display
                          Text(
                            '${provider.prepTime}s',
                            style: AppTheme.notoSansLight.copyWith(
                              fontSize: 24,
                              fontFeatures: const [
                                FontFeature.tabularFigures(),
                              ],
                              color: AppTheme.white.withValues(alpha: 0.7),
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'delay',
                            style: AppTheme.notoSansRegular.copyWith(
                              fontSize: 12,
                              letterSpacing: 1,
                              color: AppTheme.gray,
                            ),
                          ),
                          const SizedBox(height: 8),
                          // Adjust controls
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              GestureDetector(
                                onTap: provider.decrementPrepTime,
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 16,
                                    vertical: 0,
                                  ),
                                  child: Text(
                                    '−',
                                    style: AppTheme.notoSansThickerThin
                                        .copyWith(
                                          fontSize: 20,
                                          color: AppTheme.gray,
                                        ),
                                  ),
                                ),
                              ),
                              GestureDetector(
                                onTap: provider.incrementPrepTime,
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 16,
                                    vertical: 0,
                                  ),
                                  child: Text(
                                    '+',
                                    style: AppTheme.notoSansThickerThin
                                        .copyWith(
                                          fontSize: 20,
                                          color: AppTheme.gray,
                                        ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ] else ...[
                          // No prep time - show add option
                          GestureDetector(
                            onTap: provider.incrementPrepTime,
                            child: Padding(
                              padding: const EdgeInsets.all(12),
                              child: Text(
                                '+ delay',
                                style: AppTheme.notoSansLight.copyWith(
                                  fontSize: 14,
                                  letterSpacing: 1,
                                  color: AppTheme.gray.withValues(alpha: 0.8),
                                ),
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),

                // Pause Controls (Save/Clear)
                if (provider.isPaused)
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 100,
                    child: Builder(
                      builder: (context) {
                        final totalMeditationMs =
                            provider.meditationTime * 60 * 1000;
                        final elapsedMeditationTime =
                            !provider.wasPausedDuringPrep
                            ? totalMeditationMs - provider.remainingTime
                            : 0;
                        final canSave =
                            !provider.wasPausedDuringPrep &&
                            elapsedMeditationTime > 0;

                        return Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            GestureDetector(
                              onTap: provider.stopTimer,
                              child: Padding(
                                padding: const EdgeInsets.all(12),
                                child: Text(
                                  'clear',
                                  style: AppTheme.notoSansThin.copyWith(
                                    fontSize: 16,
                                    letterSpacing: 2,
                                    color: AppTheme.white,
                                  ),
                                ),
                              ),
                            ),
                            if (canSave) ...[
                              const SizedBox(width: 32),
                              GestureDetector(
                                onTap: provider.savePartialSession,
                                child: Padding(
                                  padding: const EdgeInsets.all(12),
                                  child: Text(
                                    'save',
                                    style: AppTheme.notoSansThin.copyWith(
                                      fontSize: 16,
                                      letterSpacing: 2,
                                      color: AppTheme.white,
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ],
                        );
                      },
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _formatTime(int millis) {
    // Round up to the nearest second
    final roundedMillis = millis > 0 ? millis + 999 : 0;
    final totalSeconds = roundedMillis ~/ 1000;
    final minutes = totalSeconds ~/ 60;
    final seconds = totalSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }
}
