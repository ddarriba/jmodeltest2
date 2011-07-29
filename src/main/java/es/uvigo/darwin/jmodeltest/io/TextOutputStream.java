/****************************************************************************
** COPYRIGHT (C):    1996 Cay S. Horstmann. All Rights Reserved.
** PROJECT:          Computing Concepts with Java
** FILE:             TextOutputStream.java
****************************************************************************/

/**
 * An output stream that can write formatted text
 * @version 1.00 11 Apr 1997
 * @author Cay Horstmann
 * @author Quinn Snell (BYU)

modified 21Feb05 by DP
modified 15May06 by DP
 */

package es.uvigo.darwin.jmodeltest.io;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class TextOutputStream extends OutputStream
{  

   public TextOutputStream(String s, boolean append)
   {
          try
          {
			out = new PrintWriter(new FileOutputStream(s,append));
          }
          catch(IOException e)
          {
                  out = null;
          }
   }

   public TextOutputStream(PrintStream s)
   {  out = new PrintWriter(s);
   }

   /**
    * Constructs a text output stream that sends its output to a file 
    * @param s the file name
    */

   public TextOutputStream(String s)
   {  try
      {  out = new PrintWriter(new FileOutputStream(s));
      }
      catch(IOException e)
      {  out = null;
      }
   }


   /**
    * Tests if the output stream is no longer valid
    * @return <tt>true</tt> if the stream has failed
    */

   public boolean fail()
   {  return out == null;
   }

   public boolean checkError()
   {
          return out.checkError();
   }

   public void flush()
   {
          out.flush();
   }

   /**
    * Closes the stream 
    */

   public void close()
   {  out.close();
   }

   /**
    * Writes one byte to the stream
    * @param b the byte to write
    */

   public void write(int b)
   {  out.write(b);
   }

   /**
    * Prints a string to a stream
    * @param s the string to print
    */

   public void print(String s) 
   { 
          out.print(s); 
          flush();
   }

   /**
    * Prints a character to a stream
    * @param c the character to print
    */

   public void print(char c) { out.print(c); flush(); }

   /**
    * Prints an object to a stream. The <tt>toString</tt> method of the object is invoked
    * @param o the object to print
    */

   public void print(Object o) { out.print(o);  flush();}

   /**
    * Prints a long integer to a stream
    * @param l the number to print
    */

   public void print(long l) { out.print(l); flush(); }

   /**
    * Prints a double precision floating point number to a stream
    * @param l the number to print
    */

   public void print(double x) { out.print(x);  flush();}

   /**
    * Prints a string to a stream, followed by a newline
    * @param s the string to print
    */

   public void println(String s) 
   { 
      out.println(s);
          flush();
   }

   /**
    * Prints a character to a stream, followed by a newline
    * @param c the character to print
    */

   public void println(char c) { out.println(c); flush(); }

   /**
    * Prints an object to a stream, followed by a newline. The <tt>toString</tt> method of the object is invoked
    * @param o the object to print
    */

   public void println(Object o) { out.println(o); flush(); }

   /**
    * Prints a long integer to a stream, followed by a newline
    * @param l the number to print
    */

   public void println(long l) { out.println(l); flush(); }

   /**
    * Prints a double precision floating point number to a stream, followed by a newline
    * @param l the number to print
    */

   public void println(double x) { out.println(x); flush(); }

   /**
    * Prints a newline
    */

   public void println() { out.println(); flush(); }

   private void parseFormat(String s)
   {  width = 0;
      precision = -1;
      pre = "";
      post = "";
      leading_zeroes = false;
      show_plus = false;
      alternate = false;
      show_space = false;
      left_align = false;
      fmt = ' ';

      int length = s.length();
      int parse_state = 0;
      // 0 = prefix, 1 = flags, 2 = width, 3 = precision,
      // 4 = format, 5 = end
      int i = 0;

      while (parse_state == 0)
      {  if (i >= length) parse_state = 5;
         else if (s.charAt(i) == '%')
         {  if (i < length - 1)
            {  if (s.charAt(i + 1) == '%')
               {  pre = pre + '%';
                  i++;
               }
               else
                  parse_state = 1;
            }
            else throw new java.lang.IllegalArgumentException();
         }
         else
            pre = pre + s.charAt(i);
         i++;
      }
      while (parse_state == 1)
      {  if (i >= length) parse_state = 5;
         else if (s.charAt(i) == ' ') show_space = true;
         else if (s.charAt(i) == '-') left_align = true;
         else if (s.charAt(i) == '+') show_plus = true;
         else if (s.charAt(i) == '0') leading_zeroes = true;
         else if (s.charAt(i) == '#') alternate = true;
         else { parse_state = 2; i--; }
         i++;
      }
      while (parse_state == 2)
      {  if (i >= length) parse_state = 5;
         else if ('0' <= s.charAt(i) && s.charAt(i) <= '9')
         {  width = width * 10 + s.charAt(i) - '0';
            i++;
         }
         else if (s.charAt(i) == '.')
         {  parse_state = 3;
            precision = 0;
            i++;
         }
         else
            parse_state = 4;
      }
      while (parse_state == 3)
      {  if (i >= length) parse_state = 5;
         else if ('0' <= s.charAt(i) && s.charAt(i) <= '9')
         {  precision = precision * 10 + s.charAt(i) - '0';
            i++;
         }
         else
            parse_state = 4;
      }
      if (parse_state == 4)
      {  if (i >= length) parse_state = 5;
         else fmt = s.charAt(i);
         i++;
      }
      if (i < length)
         post = s.substring(i, length);
   
   
   }

   /**
    * Prints a double precision floating point number to a stream, with formatting instructions
    * @param fmt the formatting instruction string. 
    * The string has a prefix, a format code and a suffix. The prefix and suffix
    * become part of the formatted output. The format code directs the
    * formatting of the parameter to be formatted. The code has the
    * following structure
    * <ul>
    * <li> a % (required)
    * <li> a modifier (optional)
    * <dl>
    * <dt> + <dd> forces display of + for positive numbers
    * <dt> 0 <dd> show leading zeroes
    * <dt> - <dd> align left in the field
    * <dt> space <dd> prepend a space in front of positive numbers
    * <dt> # <dd> use "alternate" format: Don't suppress trailing zeroes in general floating point format.
    * </dl>
    * <li> an integer denoting field width (optional)
    * <li> a period followed by an integer denoting precision (optional)
    * <li> a format descriptor (required)
    * <dl>
    * <dt>f <dd> floating point number in fixed format
    * <dt>e, E <dd> floating point number in exponential notation (scientific format). The E format results in an uppercase E for the exponent (1.14130E+003), the e format in a lowercase e.
    * <dt>g, G <dd> floating point number in general format (fixed format for small numbers, exponential format for large numbers). Trailing zeroes are suppressed. The G format results in an uppercase E for the exponent (if any), the g format in a lowercase e.
    * </dl>
    * </ul>
    * @param l the number to print
    */

   public void printf(String fmt, double x)
   {
 
      parseFormat(fmt);
      print(form(x));
 
   }

   /**
    * Prints a long integer to a stream, with formatting instructions
    * @param fmt the formatting instruction string. 
    * The string has a prefix, a format code and a suffix. The prefix and suffix
    * become part of the formatted output. The format code directs the
    * formatting of the parameter to be formatted. The code has the
    * following structure
    * <ul>
    * <li> a % (required)
    * <li> a modifier (optional)
    * <dl>
    * <dt> + <dd> forces display of + for positive numbers
    * <dt> 0 <dd> show leading zeroes
    * <dt> - <dd> align left in the field
    * <dt> space <dd> prepend a space in front of positive numbers
    * <dt> # <dd> use "alternate" format: Add 0 or 0x for octal or hexadecimal numbers. 
    * </dl>
    * <li> an integer denoting field width (optional)
    * <li> a format descriptor (required)
    * <dl>
    * <dt>d, i <dd> integer in decimal
    * <dt>x <dd> integer in hexadecimal
    * <dt>o <dd> integer in octal
    * </dl>
    * </ul>
    * @param x the number to print
    */

   public void printf(String fmt, long x)
   {  parseFormat(fmt);
      print(form(x));
   }

   /**
    * Prints a character to a stream, with formatting instructions
    * @param fmt the formatting instruction string. 
    * The string has a prefix, a format code and a suffix. The prefix and suffix
    * become part of the formatted output. The format code directs the
    * formatting of the parameter to be formatted. The code has the
    * following structure
    * <ul>
    * <li> a % (required)
    * <li> a modifier (optional)
    * <dl>
    * <dt> + <dd> forces display of + for positive numbers
    * <dt> 0 <dd> show leading zeroes
    * <dt> - <dd> align left in the field
    * <dt> space <dd> prepend a space in front of positive numbers
    * <dt> # <dd> use "alternate" format: Add 0 or 0x for octal or hexadecimal numbers. 
    * </dl>
    * <li> an integer denoting field width (optional)
    * <li> a format descriptor (required)
    * <dl>
    * <dt>d, i <dd> integer in decimal
    * <dt>x <dd> integer in hexadecimal
    * <dt>o <dd> integer in octal
    * <dt>c <dd> character
    * </dl>
    * </ul>
    * @param x the character to print
    */

   public void printf(String fmt, char x)
   {  parseFormat(fmt);
      print(form(x));
   }

   /**
    * Prints a string to a stream, with formatting instructions
    * @param fmt the formatting instruction string. 
    * The string has a prefix, a format code and a suffix. The prefix and suffix
    * become part of the formatted output. The format code directs the
    * formatting of the parameter to be formatted. The code has the
    * following structure
    * <ul>
    * <li> a % (required)
    * <li> a modifier (optional)
    * <dl>
    * <dt> - <dd> align left in the field
    * </dl>
    * <li> an integer denoting field width (optional)
    * <li> a format descriptor (required)
    * <dl>
    * <dt>s <dd> string
    * </dl>
    * </ul>
    * @param x the character to print
    */

   public void printf(String fmt, String x)
   {  parseFormat(fmt);
      print(form(x));
   }

   private static String repeat(char c, int n)
   {  if (n <= 0) return "";
      StringBuffer s = new StringBuffer(n);
      for (int i = 0; i < n; i++) s.append(c);
      return s.toString();
   }

   private static String convert(long x, int n, int m, String d)
   {  if (x == 0) return "0";
      String r = "";
      while (x != 0)
      {  r = d.charAt((int)(x & m)) + r;
         x = x >>> n;
      }
      return r;
   }

   private String pad(String r)
   {  String p = repeat(' ', width - r.length());
      if (left_align) return pre + r + p + post;
      else return pre + p + r + post;
   }

   private String sign(int s, String r)
   {  String p = "";
      if (s < 0) p = "-";
      else if (s > 0)
      {  if (show_plus) p = "+";
         else if (show_space) p = " ";
      }
      else
      {  if (fmt == 'o' && alternate && r.length() > 0 && r.charAt(0) != '0') p = "0";
         else if (fmt == 'x' && alternate) p = "0x";
         else if (fmt == 'X' && alternate) p = "0X";
      }
      int w = 0;
      if (leading_zeroes)
         w = width;
      else if ((fmt == 'd' || fmt == 'i' || fmt == 'x' || fmt == 'X' || fmt == 'o')
         && precision > 0) w = precision;

      return p + repeat('0', w - p.length() - r.length()) + r;
   }


   private String fixed_format(double d)
   {  String f = "";
      if (d > 0x7FFFFFFFFFFFFFFFL) return exp_format(d);

      long l = (long)(precision == 0 ? d + 0.5 : d);
      f = f + l;

      double fr = d - l; // fractional part
      if (fr >= 1 || fr < 0) return exp_format(d);


	/* DP change 210205 */
	String fraction =  frac_part(fr);
	if (fraction == "0")
	{
	int myInteger = (int) d + 1;
	String trailing_zeroes = "";
    for (int i = 1; i <= precision; i++)
        trailing_zeroes = trailing_zeroes + "0";
	// return "1." + trailing_zeroes; /* DP 150506 error here, it would print 12.99999 as 1.0!*/
		return myInteger + "." + trailing_zeroes;
	  }
	else	
    	return f + fraction;

// return f + frac_part(fr);

	/* DP */
     	
   }


/* DP 210205 : there was a problem when the precision should round to 1.0. Instead it would print 0
i.e.,	printf("%10.4f",0.99998) would print 0.0000
*/

   private String frac_part(double fr)
   // precondition: 0 <= fr < 1
   {  String z = "";
      if (precision > 0)
      {  double factor = 1;
         String leading_zeroes = "";
         for (int i = 1; i <= precision && factor <= 0x7FFFFFFFFFFFFFFFL; i++)
         {  factor *= 10;
            leading_zeroes = leading_zeroes + "0";
         }
  
        long l = (long) (factor * fr + 0.5);
    
	/* DP 210205 added */
		if (l == factor)
			return "0";

  	     z = leading_zeroes + l;
         z = z.substring(z.length() - precision, z.length());

      }

      if (precision > 0 || alternate) z = "." + z;
      
      if ((fmt == 'G' || fmt == 'g') && !alternate)
      // remove trailing zeroes and decimal point
      {  int t = z.length() - 1;
         while (t >= 0 && z.charAt(t) == '0') t--;
         if (t >= 0 && z.charAt(t) == '.') t--;
         z = z.substring(0, t + 1);
      }
      return z;
   }

   private String exp_format(double d)
   {  String f = "";
      int e = 0;
      double dd = d;
      double factor = 1;
      if (d != 0)
      {  while (dd > 10) { e++; factor /= 10; dd = dd / 10; }
         while (dd < 1) { e--; factor *= 10; dd = dd * 10; }
      }
      if ((fmt == 'g' || fmt == 'G') && e >= -4 && e < precision)
         return fixed_format(d);

      d = d * factor;
      f = f + fixed_format(d);

      if (fmt == 'e' || fmt == 'g')
         f = f + "e";
      else
         f = f + "E";

      String p = "000";
      if (e >= 0)
      {  f = f + "+";
         p = p + e;
      }
      else
      {  f = f + "-";
         p = p + (-e);
      }

      return f + p.substring(p.length() - 3, p.length());
   }

   private String form(double x)
   {  String r;
      if (precision < 0) precision = 6;
      int s = 1;
      if (x < 0) { x = -x; s = -1; }
      if (fmt == 'f')
         r = fixed_format(x);
      else if (fmt == 'e' || fmt == 'E' || fmt == 'g' || fmt == 'G')
         r = exp_format(x);
      else throw new java.lang.IllegalArgumentException();

      return pad(sign(s, r));
   }

   private String form(long x)
   {  String r;
      int s = 0;
      if (fmt == 'd' || fmt == 'i')
      {  if (x < 0) 
         {  r = ("" + x).substring(1);
            s = -1; 
         }
         else 
         {  r = "" + x; 
            s = 1;
         }
      }
      else if (fmt == 'o')
         r = convert(x, 3, 7, "01234567");
      else if (fmt == 'x')
         r = convert(x, 4, 15, "0123456789abcdef");
      else if (fmt == 'X')
         r = convert(x, 4, 15, "0123456789ABCDEF");
      else throw new java.lang.IllegalArgumentException();

      return pad(sign(s, r));
   }

   private String form(char c)
   {  if (fmt != 'c')
         throw new java.lang.IllegalArgumentException();

      String r = "" + c;
      return pad(r);
   }

   private String form(String s)
   {  if (fmt != 's')
         throw new java.lang.IllegalArgumentException();
      if (precision >= 0) s = s.substring(0, precision);
      return pad(s);
   }

   private PrintWriter out;

   private int width;
   private int precision;
   private String pre;
   private String post;
   private boolean leading_zeroes;
   private boolean show_plus;
   private boolean alternate;
   private boolean show_space;
   private boolean left_align;
   private char fmt; // one of cdeEfgGiosxXos
}
