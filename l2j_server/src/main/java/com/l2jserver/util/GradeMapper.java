package com.l2jserver.util;


public class GradeMapper {

  public static String resolveGrade(int level) {
    if (level >= 1 && level < 20) {
      return "NG";
    } else if (level >= 20 && level < 40){
      return "D";
    } else if (level >= 40 && level < 52){
      return "C";
    } else if (level >= 52 && level < 61){
      return "B";
    } else if (level >= 61 && level < 76){
      return "A";
    } else if (level >= 76 && level < 82){
      return "S";
    } else if (level >= 82){
      return "S+";
    } else {
      return "UNKNOWN";
    }
  }

}
