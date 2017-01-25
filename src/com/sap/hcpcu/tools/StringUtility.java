package com.sap.hcpcu.tools;

import java.util.ArrayList;
import java.util.List;


/**
 * Some String Utilities.
 */
public class StringUtility {
  /**
   * Return a hashcode as a positive Integer for a String.<p>
   *
   * In contrast to .hashCode(), this function returns a positive
   * Integer. This is helpful to use Strings as a unique key in
   * places (e.g. JavaScript) where you would be bothered by
   * special characters in the String.
   *
   * @param sIn The String to get a hashcode for.
   * @return The hashcode as a positive Integer in a String.
   */
  public static String hashCode(String sIn) {
    if (sIn == null) {
      return null;
    }

    return "" + Math.abs(sIn.hashCode());
  }


  /**
   * Escape quotes and linebreaks in a String.<p>
   *
   * @param sIn the String that has to be escaped
   * @return The escaped String
   */
  public static String escapeQuotes(String sIn) {
    if (sIn == null) {
      return "";
    }

    int    len = sIn.length();
    String res = "";
    char   c;
    for (int i = 0; i < len; i++) {
      c = sIn.charAt(i);
      switch (c) {
        case '\n':
          res += "\\n";

          break;

        case '\r':
          break;

        case '\\':
          res += "\\\\";

          break;

        case '"':
          res += "\\" + c;

          break;

        case '\'':
          res += "\\" + c;

          break;

        default:
          res += c;
      }
    }

    return res;
  }


  /**
   * Returns an array of string related to the key value string.<p>
   *
   * If there is nothing left or right of the separator, that position
   * is returned as an empty string:<p>
   *
   * <ul>
   *   <li><code>a/b/c</code>   returns     "a", "b", "c".    </li>
   *   <li><code>/a/b/c</code>  returns "", "a", "b", "c".    </li>
   *   <li><code>a/b/c/</code>  returns     "a", "b", "c", "".</li>
   *   <li><code>/a/b/c/</code> returns "", "a", "b", "c", "".</li>
   * <ul>
   * <p>
   * If you do not want empty Strings, you can operate
   * {@link #condense(String[])} on the return value.
   *
   * @param strParamVal the String that has to be splitted
   * @param strSep the Separator at which the String shall be splitted
   * @return Array of Strings with the Substrings.
   */
  public static String[] split(String strParamVal, String strSep) {
    if ((strSep == null) || "".equals(strSep)) {
      return new String[] { strParamVal };
    }

    String[] strRetList = new String[0];

    /*
     * This has bugged me a lot: We do not want to
     * return anything if we have an empty String.
     * Previously, as we did not find the separator,
     * the recursive algorithm returned a String
     * array of length one with the string itself.
     * If then we checked for lenght, we were
     * like thinking there was a length, while the
     * String in reality had been empty.
     */
    if ((strParamVal == null) || (strParamVal.length() == 0)) {
      return strRetList;
    }

    final int separatorPosition = strParamVal.indexOf(strSep);
    final int separatorLength   = strSep.length();

    /*
     * If we no longer have the separator,
     * we do not need to split the String
     * again.
     */
    if (separatorPosition < 0) {
      strRetList    = new String[1];
      strRetList[0] = strParamVal;

      return strRetList;
    }

    String lowValue  = "";
    String highValue = "";

    lowValue = strParamVal.substring(0, separatorPosition);
    if (strParamVal.length() >= (separatorPosition + separatorLength)) {
      highValue = strParamVal.substring((strParamVal.equals(strSep)) ? 0 : (separatorPosition + separatorLength));
    }

    final String[] recursion       = ("".equals(highValue) || strParamVal.equals(strSep)) ? new String[] { "" } : split(highValue, strSep);
    final int      recursionLength = recursion.length;

    strRetList    = new String[recursionLength + 1];
    strRetList[0] = lowValue;

    for (int i = 0; i < recursionLength; i++) {
      strRetList[i + 1] = recursion[i];
    }

    /* I rewrote this completely passing to a recursive
     * way of doing things. This cleans up considerably
     * the code. For legacy, here is the old way:

    String[] strRetList = new String[0];
    String   strVar     = "";
    int      iPos       = 0;
    int      i;
    if (strParamVal != null) { strVar = strParamVal; }

    if (strVar.length() > 0) {
      //-- Get the Number of items in strParamVal value
      int iCpt = 0;
      while (iPos >= 0) {
        iPos = strVar.indexOf(strSep);
        if (iPos >= 0) {
          strVar = strVar.substring(iPos + 1);
          iCpt   = iCpt + 1;
        } else {
          if (strVar.length() >= 0) { iCpt = iCpt + 1; }
        }
      }

      //-- Initialize the size of the list
      strRetList = new String[iCpt];

      //-- Get the list of values
      strVar     = strParamVal;
      i          = 0;
      iPos       = 0;
      while (iPos >= 0) {
        iPos = strVar.indexOf(strSep);
        if (iPos >= 0) {
          strRetList[i++] = strVar.substring(0, iPos);
          strVar          = strVar.substring(iPos + 1);
        } else {
          if (strVar.length() > 0) { strRetList[i++] = strVar; }
        }
      }
    }

    */
    return strRetList;
  }


  /**
   * Returns an array of string related to the key value string.<p>
   * An array of string is passed; every single string is split, and
   * an array containing all substrings of all strings is returned.
   *
   * @param strParamVals  the String array containing Strings that have to be splitted
   * @param strSep the Separator at which the String shall be splitted
   * @return Array of Strings with the Substrings.
   */
  public static String[] split(String[] strParamVals, String strSep) {
    int count = 0;
    if (strParamVals == null) {
      return new String[0];
    }

    for (int i = 0; i < strParamVals.length; i++) {
      String[] s = split(strParamVals[i], strSep);
      count += s.length;
    }

    String[] strRetList = new String[count];

    count = 0;
    for (int i = 0; i < strParamVals.length; i++) {
      String[] s = split(strParamVals[i], strSep);
      for (int j = 0; j < s.length; j++) {
        strRetList[count++] = s[j];
      }
    }

    return strRetList;
  }


  /**
   * Condense a String array by removing all empty Strings.
   *
   * @param strParamValues The String Array.
   * @return A String Array in the same order, but without
   *         empty Strings.
   */
  public static String[] condense(String[] strParamValues) {
    return condense(strParamValues, 0);
  }


  /**
   * Condense a String array by removing all empty Strings
   * except at the beginning the number of strings that can
   * be ignored even if they are empty.
   *
   * @param strParamValues The String Array.
   * @param ignore the number of possible empty strings that
   * may pass through at the beginning.
   * @return A String Array in the same order, but without
   *         empty Strings.
   */
  public static String[] condense(String[] strParamValues, int ignore) {
    final List<String> theValues = new ArrayList<String>();

    for (int i = 0; i < strParamValues.length; i++) {
      if ((ignore < i) || !"".equals(strParamValues[i])) {
        theValues.add(strParamValues[i]);
      }
    }

    final String[] result = new String[theValues.size()];

    int            i      = 0;
    for (final String theValue : theValues) {
      result[i++] = theValue;
    }

    return result;
  }


  /**
   * Converts an array to a string.<p>
   *
   * Only Strings that are not empty are put into the concatenated string.
   *
   * @param strParamVals  the String Array that has to be joined
   * @param strSep the Separator that has to be put between the Strings
   * @return The concatenated String
   */
  public static String join(String[] strParamVals, String strSep) {
    String strRet    = "";
    int    numValues = 0;
    if (null != strParamVals) {
      for (int i = 0; i < strParamVals.length; i++) {
        if ((strParamVals[i] != null) && (strParamVals[i].length() > 0)) {
          if (numValues == 0) {
            strRet = strParamVals[i];
          } else {
            strRet += strSep + strParamVals[i];
          }

          numValues++;
        }
      }
    }

    return strRet;
  }


  /**
   * Removes blank spaces between values and separator.<p>
   *
   * @param strPromptValue The String to be trimmed around separators
   * @param strSep the Separator the String has to be trimmed around
   * @return The updated String
   */
  public static String trimPrompt(String strPromptValue, String strSep) {
    if (null != strPromptValue) {
      String[] arrPromptValues = split(strPromptValue, strSep);
      for (int j = 0; j < arrPromptValues.length; j++) {
        if (arrPromptValues[j] != null) {
          arrPromptValues[j] = arrPromptValues[j].trim();
        }
      }

      strPromptValue = join(arrPromptValues, strSep);
    }

    return strPromptValue;
  }


  /**
   * Replaces String into a text.<p>
   *
   * @param strText The Text in which the substitution shall be done
   * @param strOldString The String that has to be replaced
   * @param strNewString The replacement String
   * @param blnAll true, if all matches shall be replaced, false for only first match.
   * @return The updated String
   */
  public static String replace(String strText, String strOldString, String strNewString, boolean blnAll) {
    if (null != strText) {
      int iLength = strOldString.length();
      int iPos    = 0;
      while (iPos >= 0) {
        iPos = strText.indexOf(strOldString);
        if (iPos >= 0) {
          String strLeft  = strText.substring(0, iPos);
          String strRight = strText.substring(iPos + iLength);
          strText =  strLeft + strNewString + strRight;
          iPos    += iLength;
          if (!blnAll) {
            break;
          }
        }
      }
    }

    return strText;
  }


  /**
   * Strip leading String from a String.<p>
   *
   * Example: From <code>/HCPCU</code>, strip
   * the <code>/</code>, so that you get <code>BOInterface</code>.<p>
   *
   * If there are multiple occurrences at the beginning,
   * they are all suppressed.
   *
   * @param strText The String to strip from.
   * @param toStrip The Fraction to suppress.
   * @return String where the leading part was eliminated.
   */
  public static String stripLeading(String strText, String toStrip) {
    String stripped = strText;

    while (stripped.startsWith(toStrip)) {
      stripped = stripped.substring(toStrip.length());
    }

    return stripped;
  }


  /**
   * Strip trailing String from a String.<p>
   *
   * Example: From <code>HCPCU/</code>, strip
   * the <code>/</code>, so that you get <code>BOInterface</code>.<p>
   *
   * If there are multiple occurrences at the beginning,
   * they are all suppressed.
   *
   * @param strText The String to strip from.
   * @param toStrip The Fraction to suppress.
   * @return String where the trailing part was eliminated.
   */
  public static String stripTrailing(String strText, String toStrip) {
    String stripped = strText;

    while (stripped.endsWith(toStrip)) {
      stripped = stripped.substring(0, stripped.length() - toStrip.length());
    }

    return stripped;
  }


  /**
   * Strip leading and trailing String from a String.<p>
   *
   * Example: From <code>/HCPCU/</code>, strip
   * the <code>/</code>, so that you get <code>BOInterface</code>.<p>
   *
   * If there are multiple occurrences at the beginning or end,
   * they are all suppressed.
   *
   * @param strText The String to strip from.
   * @param toStrip The Fraction to suppress.
   * @return String where the leading and trailing parts were eliminated.
   */
  public static String stripBoth(String strText, String toStrip) {
    return StringUtility.stripLeading(StringUtility.stripTrailing(strText, toStrip), toStrip);
  }


  /**
   * Parse year, month, day out of a date string.<p>
   *
   * @param strDate The Date String that is to be parsed, e.g. "2004-12-30"
   * @param strFormat The Date Format pattern, e.g. "y-mm-dd". The following
   *        pattern entries are possible: "y" for 4-digit year, "yy" for two-digit year;
   *        "mm" for the month number, zero-padded;
   *        "dd" for the day number, zero-padded.
   * @return String Array with the following entries:
   * <ul>
   *   <li>[ 0]=Year</li>
   *   <li>[ 1]=Month</li>
   *   <li>[ 2]=Day</li>
   * </ul>
   */
  public static String[] parseDate(String strDate, String strFormat) {
    /* get the year positions */
    int yrStart = strFormat.indexOf("y");
    int yrEnd   = strFormat.lastIndexOf("y");
    int yrComp  = 0;
    if ((yrStart < 0) || (yrEnd < 0)) {
      return new String[0];
    }

    if (yrStart == yrEnd) {
      yrComp = 3;
    }

    yrEnd += yrComp + 1;

    /* get the month positions */
    int moStart = strFormat.indexOf("m");
    int moEnd   = strFormat.lastIndexOf("m");
    if ((moStart < 0) || (moEnd < 0)) {
      return new String[0];
    }

    if (moStart > yrStart) {
      moStart += yrComp;
      moEnd   += yrComp + 1;
    }

    /* get the day positions */
    int dyStart = strFormat.indexOf("d");
    int dyEnd   = strFormat.lastIndexOf("d");
    if ((dyStart < 0) || (dyEnd < 0)) {
      return new String[0];
    }

    if (dyStart > yrStart) {
      dyStart += yrComp;
      dyEnd   += yrComp + 1;
    }

    String[] ret = new String[3];
    ret[0] = strDate.substring(yrStart, yrEnd);
    ret[1] = strDate.substring(moStart, moEnd);
    ret[2] = strDate.substring(dyStart, dyEnd);

    return ret;
  }


  /**
   * Convert a String into an Integer and return 0 if this is not possible.<p>
   *
   * @param strString The String that has to be converted
   * @return The Integer Value
   */
  public static int StringToInteger(String strString) {
    try {
      return Integer.parseInt(strString);
    } catch (NumberFormatException e) {
      return 0;
    }
  }


  /**
   * Convert a String into an Integer and return a default value if this not possible.<p>
   *
   * @param strString The String that has to be converted
   * @param intDefault The default value that shall be returned on error
   * @return The Integer Value
   */
  public static int StringToInteger(String strString, int intDefault) {
    try {
      return Integer.parseInt(strString);
    } catch (NumberFormatException e) {
      return intDefault;
    }
  }


  /**
   * Return a non null value for the given string.<p>
   *
   * @param strString The String that has to be checked against null
   * @param strDefaultValue The default value in case the String is null
   * @return The checked String.
   */
  public static String getNonNullValue(String strString, String strDefaultValue) {
    if (strString == null) {
      strString = strDefaultValue;
    } else if ("null".equals(strString)) {
      strString = strDefaultValue;
    }

    return strString;
  }


  /**
   * Return a non null value for the given string,
   * considering that default value is an empty string.<p>
   *
   * @param strString The String that has to be checked against null
   * @return The checked String.
   */
  public static String getNonNullValue(String strString) {
    return getNonNullValue(strString, "");
  }


  /**
   * Return an array of non null values for the given string array.<p>
   *
   * @param strString The String array that has to be checked against null
   * @return The checked String array.
   */
  public static String[] getNonNullValues(String[] strString) {
    if (strString == null) {
      strString = new String[0];
    }

    return strString;
  }


  /**
   * Get the name or the value of a parameter. The parameter may look like:
   * <p>
   * <xmp>
   *   aname=bvalue
   *   -x=y
   *   --parameter=value
   * </xmp>
   * <p>
   * @param parameter A String as shown in the above examples.
   * @param name true if the name is required, false if the value is required.
   * @param casesensitive false if the parameters are to be converted to all upper case, else true.
   * @return Name or value, depending on the name parameter; null if an error occurred.
   */
  public static String getParameter(String parameter, boolean name, boolean casesensitive) {
    if (parameter == null) {
      return null;
    }

    String[] parametertokens = split(parameter, "=");

    if ((parametertokens == null) || ((parametertokens.length < 1) && name) || ((parametertokens.length < 2) && !name)) {
      return null;
    }

    if (name) {
      String parametername = parametertokens[0];
      while ("-".equals(parametername.substring(0, 1))) {
        parametername = parametername.substring(1);
      }

      if (casesensitive) {
        return parametername;
      } else {
        return parametername.toUpperCase();
      }
    }

    return parametertokens[1];
  }


  /**
   * URLEncode a String.
   *
   * This is a proxy to java.net.URLEncoder.encode(), as the
   * one parameter version is deprecated and the two parameter
   * version throws an UnsupportedEncodingException which we
   * normally never run into.
   *
   * @param s The String to encode.
   * @return The encoded String.
   */
  public static String URLEncode(String s) {
    //    /*
    //     * JDK 1.3.1
    //     */
    //    return java.net.URLEncoder.encode(s);

    /*
     * JDK 1.4.2
     */
    try {
      return java.net.URLEncoder.encode(s, "UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      return s;
    }
  }


  /**
   * URLDecode a String.
   *
   * This is a proxy to java.net.URLDecoder.decode(), as the
   * one parameter version is deprecated and the two parameter
   * version throws an UnsupportedEncodingException which we
   * normally never run into.
   *
   * @param s The String to decode.
   * @return The decoded String.
   */
  public static String URLDecode(String s) {
    //    /*
    //     * JDK 1.3.1
    //     */
    //    return java.net.URLDecoder.decode(s);

    /*
     * JDK 1.4.2
     */
    try {
      return java.net.URLDecoder.decode(s, "UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      return s;
    }
  }
}
