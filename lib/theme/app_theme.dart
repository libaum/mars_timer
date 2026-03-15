import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTheme {
  static const Color black = Color(0xFF000000);
  static const Color white = Color(0xFFFFFFFF);
  static const Color gray = Color(0xFF808080);
  static const Color darkGray = Color(0xFF404040);

  static TextStyle get notoSansLight => GoogleFonts.notoSans(
        fontWeight: FontWeight.w300,
      );

  static TextStyle get notoSansRegular => GoogleFonts.notoSans(
        fontWeight: FontWeight.w400,
      );

  static TextStyle get notoSansMedium => GoogleFonts.notoSans(
        fontWeight: FontWeight.w500,
      );

  static TextStyle get notoSansThin => GoogleFonts.notoSans(
        fontWeight: FontWeight.w100,
      );

  static TextStyle get notoSansThickerThin => GoogleFonts.notoSans(
    fontWeight: FontWeight.w200,
  );

  static ThemeData get darkTheme => ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: black,
        colorScheme: const ColorScheme.dark(
          primary: white,
          secondary: white,
          tertiary: white,
          surface: black,
          onPrimary: black,
          onSecondary: black,
          onTertiary: black,
          onSurface: white,
        ),
        textTheme: TextTheme(
          displayLarge: notoSansLight.copyWith(fontSize: 57, color: white),
          displayMedium: notoSansLight.copyWith(fontSize: 45, color: white),
          titleMedium: notoSansMedium.copyWith(fontSize: 16, color: white),
          bodyLarge: notoSansRegular.copyWith(
            fontSize: 16,
            color: white,
            height: 1.5,
            letterSpacing: 0.5,
          ),
          labelMedium: notoSansMedium.copyWith(
            fontSize: 12,
            color: white,
            letterSpacing: 2,
          ),
        ),
        useMaterial3: true,
      );
}

