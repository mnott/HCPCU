package com.sap.hcpcu.tools;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Read  CSV (Comma Separated Value)
 * files.
 *
 * This format is used by Microsoft Word and Excel.
 * Fields are separated by commas, and enclosed in
 * quotes if they contain commas or quotes.
 * Embedded quotes are doubled.
 * Embedded spaces do not normally require surrounding quotes.
 * The last field on the line is not followed by a comma.
 * Null fields are represented by two commas in a row.
 * We ignore leading and trailing spaces on fields, even inside quotes.
 */
public class CSVReader {
  private static final boolean debugging     = true;


  /**
   * category of end of line char.
   */
  private static final int     EOL           = 0;

  /**
   * category of ordinary character
   */
  private static final int     ORDINARY      = 1;

  /**
   * categotory of the quote mark "
   */
  private static final int     QUOTE         = 2;

  /**
   * category of the separator, e.g. comma, semicolon
   * or tab.
   */
  private static final int     SEPARATOR     = 3;

  /**
   * category of characters treated as white space.
   */
  private static final int     WHITESPACE    = 4;

  /**
   * parser: We are in blanks before the field.
   */
  private static final int     SEEKINGSTART  = 0;

  /**
   * parser: We are in the middle of an ordinary field.
   */
  private static final int     INPLAIN       = 1;

  /**
   * parser: e are in middle of field surrounded in quotes.
   */
  private static final int     INQUOTED      = 2;

  /**
   * parser: We have just hit a quote, might be doubled
   * or might be last one.
   */
  private static final int     AFTERENDQUOTE = 3;

  /**
   * parser: We are in blanks after the field looking for the separator
   */
  private static final int     SKIPPINGTAIL  = 4;

  /**
   * Reader source of the CSV fields to be read.
   */
  private BufferedReader       r;

  /*
   * field separator character, usually ',' in North America,
   * ';' in Europe and sometimes '\t' for tab.
   */
  private char                 separator;

  /**
   * state of the parser's finite state automaton.
   */

  /**
   * The line we are parsing.
   * null means none read yet.
   * Line contains unprocessed chars. Processed ones are removed.
   */
  private String               line          = null;

  /**
   * How many lines we have read so far.
   * Used in error messages.
   */
  private int                  lineCount     = 0;

  /**
   * Constructor
   *
   * @param r input Reader source of CSV Fields to read.
   * @param separator field separator character, usually ',' in North America,
   *               ';' in Europe and sometimes '\t' for tab.
   */
  public CSVReader(Reader r, char separator) {
    /* convert Reader to BufferedReader if necessary */
    if (r instanceof BufferedReader) {
      this.r = (BufferedReader) r;
    } else {
      this.r = new BufferedReader(r);
    }

    this.separator = separator;
  }

  /**
   * categorise a character for the finite state machine.
   *
   * @param c      the character to categorise
   * @return integer representing the character's category.
   */
  private int categorise(char c) {
    switch (c) {
      case ' ':
      case '\r':
      case 0xff:
        return WHITESPACE;

      case '\n':
        return EOL; /* artificially applied to end of line */

      case '\"':
        return QUOTE;

      default:
        if (c == separator /* dynamically determined so can't use as case label */) {
          return SEPARATOR;
        } /* do our tests in crafted order, hoping for an early return */
        else if (('!' <= c) && (c <= '~')) {
          return ORDINARY;
        } else if ((0x00 <= c) && (c <= 0x20)) {
          return WHITESPACE;
        } else if (Character.isWhitespace(c)) {
          return WHITESPACE;
        } else {
          return ORDINARY;
        }
    }
  }


  /**
   * Read one field from the CSV file
   *
   * @return String value, even if the field is numeric.  Surrounded
   *         and embedded double quotes are stripped.
   *         possibly "".  null means end of line.
   *
   * @exception EOFException
   *                   at end of file after all the fields have
   *                   been read.
   *
   * @exception IOException
   *                   Some problem reading the file, possibly malformed data.
   */
  public String get() throws EOFException, IOException {
    StringBuffer field = new StringBuffer(50);

    /* we implement the parser as a finite state automaton with five states. */
    getLine();

    int state = SEEKINGSTART; /* start seeking, even if partway through a line */
    /* don't need to maintain state between fields. */
    /* loop for each char in the line to find a field */
    /* guaranteed to leave early by hitting EOL */
    for (int i = 0; i < line.length(); i++) {
      char c        = line.charAt(i);
      int  category = categorise(c);
      switch (state) {
        case SEEKINGSTART: { /* in blanks before field */
          switch (category) {
            case WHITESPACE: /* ignore */
              break;

            case QUOTE:
              state = INQUOTED;

              break;

            case SEPARATOR: /* end of empty field */
              line = line.substring(i + 1);

              return "";

            case EOL: /* end of line */
              line = null;

              return null;

            case ORDINARY:
              field.append(c);
              state = INPLAIN;

              break;
          }

          break;
        }

        case INPLAIN: { /* in middle of ordinary field */
          switch (category) {
            case QUOTE:
              throw new IOException("Malformed CSV stream. Missing quote at start of field on line " + lineCount);

            case SEPARATOR: /* done */
              line = line.substring(i + 1);

              return field.toString().trim();

            case EOL:
              line = line.substring(i); /* push EOL back */

              return field.toString().trim();

            case WHITESPACE:
              field.append(' ');

              break;

            case ORDINARY:
              field.append(c);

              break;
          }

          break;
        }

        case INQUOTED: { /* in middle of field surrounded in quotes */
          switch (category) {
            case QUOTE:
              state = AFTERENDQUOTE;

              break;

            case EOL:
              throw new IOException("Malformed CSV stream. Missing quote after field on line " + lineCount);

            case WHITESPACE:
              field.append(' ');

              break;

            case SEPARATOR:
            case ORDINARY:
              field.append(c);

              break;
          }

          break;
        }

        case AFTERENDQUOTE: {
          /* In situation like this "xxx" which may
             turn out to be xxx""xxx" or "xxx",
             We find out here. */
          switch (category) {
            case QUOTE:
              field.append(c);
              state = INQUOTED;

              break;

            case SEPARATOR: /* we are done.*/
              line = line.substring(i + 1);

              return field.toString().trim();

            case EOL:
              line = line.substring(i); /* push back eol */

              return field.toString().trim();

            case WHITESPACE: /* ignore trailing spaces up to separator */
              state = SKIPPINGTAIL;

              break;

            case ORDINARY:
              throw new IOException("Malformed CSV stream, missing separator after fieldon line " + lineCount);
          }

          break;
        }

        case SKIPPINGTAIL: { /* in spaces after field seeking separator */
          switch (category) {
            case SEPARATOR: /* we are done.*/
              line = line.substring(i + 1);

              return field.toString().trim();

            case EOL:
              line = line.substring(i); /* push back eol */

              return field.toString().trim();

            case WHITESPACE: /* ignore trailing spaces up to separator */
              break;

            case QUOTE:
            case ORDINARY:
              throw new IOException("Malformed CSV stream, missing separator after field on line " + lineCount);
          }

          break;
        }
      } // end switch(state)
    } // end for

    throw new IOException("Program logic bug. Should not reach here. Processing line " + lineCount);
  } // end get


  /**
   * Make sure a line is available for parsing.
   *  Does nothing if there already is one.
   *
   * @exception EOFException
   */
  private void getLine() throws EOFException, IOException {
    if (line == null) {
      line = r.readLine(); /* this strips platform specific line ending */
      if (line == null) { /* null means EOF, yet another inconsistent Java convention. */
        throw new EOFException();
      } else {
        line += '\n'; /* apply standard line end for parser to find */
        lineCount++;
      }
    }
  }


  /**
   * Skip over fields you don't want to process.
   *
   * @param fields How many field you want to bypass reading.
   *               The newline counts as one field.
   * @exception EOFException
   *               at end of file after all the fields have
   *               been read.
   * @exception IOException
   *               Some problem reading the file, possibly malformed data.
   */
  public void skip(int fields) throws EOFException, IOException {
    if (fields <= 0) {
      return;
    }

    for (int i = 0; i < fields; i++)
      get(); // throw results away
  }


  /**
   * Skip over remaining fields on this line you don't want to process.
   *
   * @exception EOFException
   *                   at end of file after all the fields have
   *                   been read.
   * @exception IOException
   *                   Some problem reading the file, possibly malformed data.
   */
  public void skipToNextLine() throws EOFException, IOException {
    if (line == null) {
      getLine();
    }

    line = null;
  }


  /**
   * Close the Reader.
   */
  public void close() throws IOException {
    if (r != null) {
      r.close();
      r = null;
    }
  }


  /**
   * Test driver
   *
   * @param args   not used
   */
  static public void main(String[] args) {
    if (debugging) {
      try { // read test file

        CSVReader csv = new CSVReader(new FileReader("users.txt"), ';');
        try {
          while (true) {
            String s = csv.get();
            if (s != null) {
              System.out.println(s);
            }
          }
        } catch (EOFException e) {}

        csv.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
      }
    } // end if
  } // end main
} // end CSVReader class.
