/**
 *  This file is part of ytd2
 *
 *  ytd2 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ytd2 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with ytd2.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ytd2;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * knoedel@section60:~/workspace/ytd2$ echo " *" `egrep -v
 * "(^\s*(\/\*|\*|//)|^\s
 * *$)" src/zsk/*java | wc -l` java code lines && echo -e "
 * *" `egrep "(^\s*(\/\*
 * |\*|//)|^\s*$)" src/zsk/*java | wc -l` empty/comment lines "\n *" 1077 java
 * code lines 432 empty/comment lines
 * 
 * knoedel@section60:~/workspace/ytd2$ date && uname -a && cat /etc/*rele* &&
 * java -version Mon Aug 29 22:39:47 CEST 2011 Linux section60 2.6.38-11-generic
 * #48-Ubuntu SMP Fri Jul 29 19:02:55 UTC 2011 x86_64 x86_64 x86_64 GNU/Linux
 * DISTRIB_ID=Ubuntu DISTRIB_RELEASE=11.04 DISTRIB_CODENAME=natty
 * DISTRIB_DESCRIPTION="Ubuntu 11.04" java version "1.6.0_26" Java(TM) SE
 * Runtime Environment (build 1.6.0_26-b03) Java HotSpot(TM) 64-Bit Server VM
 * (build 20.1-b02, mixed mode)
 * 
 * 
 * http://www.youtube.com/watch?v=5nj77mJlzrc <meta name="title"
 * content="BF109 G"> In lovely memory of my grandpa, who used to fly around the
 * clouds. http://www.youtube.com/watch?v=I3lq1yQo8OY&NR=1&feature=fvwp <meta
 * name="title" content="Showdown: Air Combat - Me-109">
 * http://www.youtube.com/watch?v=yxXBhKJnRR8
 * http://www.youtube.com/watch?v=RYXd60D_kgQ&feature=related <meta name="title"
 * content="Me 262 Flys Again!">
 * http://www.youtube.com/watch?v=6ejc9_yR5oQ&feature=related <meta name="title"
 * content="Focke Wulf 190 attacks Boeing B 17 in 2009 at Hahnweide">
 * 
 * technobase.fm / We Are One!
 * 
 * using Eclipse 3.5/3.6/3.7 TODOs are for Eclipse IDE - Tasks View
 * 
 * tested on GNU/Linux JRE 1.6.0_24 64bit, M$-Windows XP 64bit JRE 1.6.0_22
 * 32&64Bit and M$-Windows 7 32Bit JRE 1.6.0_23 32Bit using Mozilla Firefox
 * 3.6-6 and M$-IE (8)
 * 
 * source code compliance level is 1.5 java files are UTF-8 encoded javac shows
 * no warning java code could be easily converted to Java 1.4.2
 */
public class YTD2 {
    public static final String szVersion = "V20110922_2202 by MrKnödelmann";

    // more or less (internal) output
    boolean bDEBUG = false;

    // just report file size of HTTP header - don't download binary data (the
    // video)
    boolean bNODOWNLOAD = false;

    public static String sproxy = null;

    public static String szDLSTATE = "downloading ";

    // TODO download with cli only? does this make sense if its all about
    // videos?!

    // something like
    // [http://][www.]youtube.[cc|to|pl|ev|do|ma|in]/watch?v=0123456789A
    public static final String szYTREGEX = "^((H|h)(T|t)(T|t)(P|p)://)?((W|w)(W|w)(W|w)\\.)?(Y|y)(O|o)(U|u)(T|t)(U|u)(B|b)(E|e)\\..{2,5}/(W|w)(A|a)(T|t)(C|c)(H|h)\\?(v|V)=[^&]{11}"; // http://de.wikipedia.org/wiki/CcTLD
    // something like [http://][*].youtube.[cc|to|pl|ev|do|ma|in]/ the last / is
    // for marking the end of host, it does not belong to the hostpart
    public static final String szYTHOSTREGEX = "^((H|h)(T|t)(T|t)(P|p)://)?(.*)\\.(Y|y)(O|o)(U|u)(T|t)(U|u)(B|b)(E|e)\\..{2,5}/";

    // RFC-1123 ? hostname [with protocol]
    // public static final String szPROXYREGEX =
    // "^((H|h)(T|t)(T|t)(P|p)://)?([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
    public static final String szPROXYREGEX = "(^((H|h)(T|t)(T|t)(P|p)://)?([a-zA-Z0-9]+:[a-zA-Z0-9]+@)?([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*(:[0-90-90-90-9]{1,4})?$)|()";

    private static final String szPLAYLISTREGEX = "/view_play_list\\?p=([A-Za-z0-9]*)&playnext=[0-9]{1,2}&v=";

    // all characters that do not belong to an HTTP URL - could be written
    // shorter?? (where did I used this?? dont now anymore)
    final String snotsourcecodeurl = "[^(a-z)^(A-Z)^(0-9)^%^&^=^\\.^:^/^\\?^_^-]";

    private Boolean bQuitrequested = false;

    YTDownloadThread t1;

    public synchronized Boolean getbQuitrequested() {
        return bQuitrequested;
    }

    public synchronized void setbQuitrequested(Boolean bQuitrequested) {
        this.bQuitrequested = bQuitrequested;
    }

    // always HD
    public synchronized int getIdlbuttonstate() {
        // 4 - hd
        // 2 - st
        // 1 - low
        return 4;
    }

    public void shutdownAppl() {
        // running downloads are difficult to terminate (Thread.isInterrupted()
        // does not work there)
        synchronized (bQuitrequested) {
            bQuitrequested = true;
        }
        try {
            try {
                t1.interrupt();
            } catch (NullPointerException npe) {
            }
            try {
                t1.join();
            } catch (NullPointerException npe) {
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    } // shutdownAppl()

    /**
     * @param string
     * @param regex
     * @param replaceWith
     * @return changed String
     */
    String replaceAll(String string, String regex, String replaceWith) {
        Pattern myPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return (myPattern.matcher(string).replaceAll(replaceWith));
    } // replaceAll

    synchronized void setbNODOWNLOAD(boolean bNODOWNLOAD) {
        this.bNODOWNLOAD = bNODOWNLOAD;
    } // setbNODOWNLOAD

    synchronized boolean getbNODOWNLOAD() {
        return (bNODOWNLOAD);
    } // getbNODOWNLOAD

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event dispatch thread.
     */
    void create() {
        YTD2.sproxy = System.getenv("http_proxy");
        if (YTD2.sproxy == null)
            sproxy = "";

    }

    void download(String url, String sdirectory) {
        // lets respect the upload limit of google (youtube)
        // downloading is faster than viewing anyway so don't start more than
        // four threads and don't play around with the URL-strings please!!!
        t1 = new YTDownloadThread(bDEBUG, sdirectory, this, url);
    }

    /**
     * check if a youtube-URL was pasted or typed in if yes cut it out and send
     * it to the URLList to get processed by one of the threads
     * 
     * the user can paste a long string containing many youtube-URLs .. but here
     * is work to do because we have to erase the string(s) that remain(s)
     */
    void checkInputFieldforYTURLs(String sinput, String sdirectory) {
        // TODO this can probably be done better - replace input so URLs get
        // extracted without user activity (works even if URLs are spread across
        // multiple lines and pasted at once)
        sinput = sinput.replaceAll("&feature=fvwp&", "&"); // after that text
                                                           // there could be
                                                           // another yt-URL or
                                                           // more query_string
                                                           // options
        sinput = sinput.replaceAll("&feature=fvwphttp", "http");
        sinput = sinput.replaceAll("&feature=fvwp", "");
        sinput = sinput.replaceAll("&feature=related&", "&");
        sinput = sinput.replaceAll("&feature=relatedhttp", "http");
        sinput = sinput.replaceAll("&feature=related", "");
        sinput = sinput.replaceAll("&feature=mfu_in_order&list=[0-9A-Z]{1,2}", "");
        sinput = sinput.replaceAll("&feature=[a-zA-Z]{1,2}&list=([a-zA-Z0-9]*)&index=[0-9]{1,2}", "");
        sinput = sinput.replaceAll("&feature=[0-9A-Z]{1,3}&list=(PL[a-zA-Z0-9]{16})&index=[0-9]{1,2}", "");
        sinput = sinput.replaceAll("&playnext=[0-9A-Z]{1,2}&list=(PL[a-zA-Z0-9]{16})", "");
        sinput = sinput.replaceAll("&NR=[0-9]&", "&");
        sinput = sinput.replaceAll("&NR=[0-9]http", "http");
        sinput = sinput.replaceAll("&NR=[0-9]", "");
        sinput = sinput.replaceAll(" ", "");
        sinput = sinput.replaceAll(szPLAYLISTREGEX, "/watch?v=");

        String surl = sinput.replaceFirst(szYTREGEX, "");

        // if nothing could be replaced we have to yt-URL found
        if (sinput.equals(surl))
            return;

        // starting at index 0 because szYTREGEX should start with ^ // if
        // szYTREGEX does not start with ^ then you have to find the index where
        // the match is before you can cut out the URL
        surl = sinput.substring(0, sinput.length() - surl.length());
        download(surl, sdirectory);
        sinput = sinput.substring(surl.length());

        // if remaining text is shorter than shortest possible yt-url we delete
        // it
        if (sinput.length() < "youtube.com/watch?v=0123456789a".length())
            sinput = "";

        // frame.textinputfield.setText(sinput); // generates a
        // java.lang.IllegalStateException: Attempt to mutate in notification

    } // checkInputFieldforYTURLS

    ArrayList<Listener> list = new ArrayList<YTD2.Listener>();

    public static interface Listener {
        public void changed();
    }

    void changed() {
        for (Listener l : list) {
            l.changed();
        }
    }

    public YTD2(String source, String target) {
        super();

        create();

        checkInputFieldforYTURLs(source, target);
    }

    public void start() {
        t1.start();
    }

    public void stop() {
        t1.interrupt();
    }

    public boolean isActive() {
        return t1.isAlive();
    }

    public Exception getException() {
        synchronized (t1.statsLock) {
            return t1.e;
        }
    }

    public void close() {
        shutdownAppl();
    }

    public String getInput() {
        synchronized (t1.statsLock) {
            return t1.input;
        }
    }

    public String getOutput() {
        synchronized (t1.statsLock) {
            return t1.sFileName;
        }
    }

    public long getBytes() {
        synchronized (t1.statsLock) {
            return t1.count;
        }
    }

    public long getTotal() {
        synchronized (t1.statsLock) {
            return t1.total;
        }
    }

    public String getTitle() {
        synchronized (t1.statsLock) {
            return t1.getTitle();
        }
    }

    /**
     * Please not by using listener you agree to handle multithread calls. I
     * suggest if you do SwingUtils.invokeLater (or your current thread manager)
     * for each changed event.
     * 
     * @param l
     *            listenrer
     */
    public void addListener(Listener l) {
        list.add(l);
    }

    public void removeListener(Listener l) {
        list.remove(l);
    }

    public static void main(String[] args) {
        YTD2 y = new YTD2("http://www.youtube.com/watch?v=svJvT6ruolA&feature=relmfu", "/Users/axet/Downloads");
        y.start();

        System.out.println("input: " + y.getInput());

        while (y.isActive()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            System.out.println("title: " + y.getTitle() + " bytes: " + y.getBytes() + " total: " + y.getTotal());
        }

        if (y.getException() != null)
            y.getException().printStackTrace();
    }
}
