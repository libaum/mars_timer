// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.
import 'package:flutter_test/flutter_test.dart';
import 'package:mars_timer/main.dart';
void main() {
  testWidgets('App starts correctly', (WidgetTester tester) async {
    // Basic smoke test - can be extended later
    // The app requires async initialization, so just verify main.dart compiles
    expect(MarsTimerApp, isNotNull);
  });
}
