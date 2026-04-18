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
          if (provider.isIdle) {
            provider.startTimer();
          } else if (provider.isRunning) {
            provider.pauseTimer();
          } else if (provider.isPaused) {
            provider.resumeTimer();
          }
          // finished state: no tap action — use the overtime counter or buttons below
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
                                alpha: provider.timerState == TimerState.idle ? 0.5 : 0,
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
                                alpha: provider.timerState == TimerState.idle ? 0.5 : 0,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),

                // Quick Select Buttons (only in Idle)
                if (provider.timerState == TimerState.idle)
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
                        ...provider.quickSelectSlots.asMap().entries.map((entry) {
                          final index = entry.key;
                          final time = entry.value;
                          final isSelected = provider.meditationTime == time;
                          return GestureDetector(
                            onTap: () => provider.setMeditationTime(time),
                            onLongPress: () => _editSlot(context, provider, index),
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
                        }),
                      ],
                    ),
                  ),

                // Prep Time Controls (only in Idle)
                if (provider.timerState == TimerState.idle)
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

                // Finished State — Overtime Counter
                if (provider.isFinished)
                  Positioned(
                    left: 0,
                    right: 0,
                    top: screenHeight * 0.52 - statusBarHeight,
                    child: GestureDetector(
                      onTap: provider.overtimeAccepted
                          ? null
                          : provider.acceptOvertime,
                      child: Center(
                        child: Text(
                          _formatOvertime(provider.overtimeMs),
                          style: AppTheme.notoSansThin.copyWith(
                            fontSize: 36,
                            fontFeatures: const [FontFeature.tabularFigures()],
                            color: provider.overtimeAccepted
                                ? AppTheme.gray
                                : AppTheme.white.withValues(alpha: 0.5),
                          ),
                        ),
                      ),
                    ),
                  ),

                // Finished State — Discard / Save
                if (provider.isFinished)
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 100,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        GestureDetector(
                          onTap: provider.discardFinishedSession,
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Text(
                              'discard',
                              style: AppTheme.notoSansThin.copyWith(
                                fontSize: 16,
                                letterSpacing: 2,
                                color: AppTheme.white,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 32),
                        GestureDetector(
                          onTap: provider.saveFinishedSession,
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
                        final elapsedMeditationTime =
                            !provider.wasPausedDuringPrep
                            ? provider.totalMeditationMs - provider.remainingTime
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

  void _editSlot(BuildContext context, TimerProvider provider, int index) {
    final controller = TextEditingController(
      text: '${provider.quickSelectSlots[index]}',
    );
    showDialog(
      context: context,
      builder: (ctx) => Dialog(
        backgroundColor: AppTheme.darkGray,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 28),
          child: TextField(
            controller: controller,
            keyboardType: TextInputType.number,
            autofocus: true,
            textAlign: TextAlign.center,
            style: AppTheme.notoSansThin.copyWith(
              fontSize: 48,
              color: AppTheme.white,
              fontFeatures: const [FontFeature.tabularFigures()],
            ),
            decoration: InputDecoration(
              border: InputBorder.none,
              hintText: '—',
              hintStyle: AppTheme.notoSansThin.copyWith(
                fontSize: 48,
                color: AppTheme.gray,
              ),
              suffixText: 'min',
              suffixStyle: AppTheme.notoSansLight.copyWith(
                fontSize: 16,
                color: AppTheme.gray,
              ),
            ),
            onSubmitted: (value) {
              final minutes = int.tryParse(value);
              if (minutes != null && minutes > 0) {
                provider.setQuickSelectSlot(index, minutes);
              }
              Navigator.of(ctx).pop();
            },
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

  String _formatOvertime(int millis) {
    final totalSeconds = millis ~/ 1000;
    final minutes = totalSeconds ~/ 60;
    final seconds = totalSeconds % 60;
    return '+${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }
}
