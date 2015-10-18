/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderazzi.filters.examples.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class TestData {

    private static Random random = new Random();

    static final String maleNames[] = { // 50 names
            /* specifically lower case >*/ "alfred",
            /*-*/ "Alvin", "Blake", "BOb", "Brandon",
            /* to check ignore case */ "BUd", "Burton",
            /*-*/ "Charles", "Clark", "Dale", "Damon", "Darren", "Dustin",
            /*-*/ "edward", "Elton", "Fletcher", "forrester", "Gary", "Harley",
            /*-*/ "Harold", "Hugh", "James", "Keane", "Kenneth", "Landon",
            /* to verify unicode */ "Ländon", "LÄndon",
            /* to verify inclusion of operators on name */ ">Lee",
            /*-*/ "Lincoln", "Maxwell", "Miller", "Nash", "Nelson", "Norman",
            /*-*/ "Oswald", "Perry", "Prentice",
            /* to verify inclusion of operators on name */ "=Ralph",
            /*-*/ "Raymond", "Richard", "Robert", "Scott", "Spencer", "Stanley",
            /* to verify impact of wildcards on name */ "Sut*ton",
            /*-*/ "Taylor", "Thorne", "Truman", "Tyler", "Wallace",
        };

    static final String femaleNames[] = { // 49 names
            "Aida", "Ashley", "Audrey", "Beverly", "Brenda", "Brook", "Cameron",
            "Carling", "Chelsea", "Dale", "Dawn", "Devon", "Dustin", "Erika",
            "Farrah", "Harmony", "Hazel", "Heather", "Holly", "Jamie", "Joyce",
            "Joy", "Kim", "Kirsten", "Kyla", "Lark", "Lee", "Leigh", "Leslie",
            "Lindsay", "Mercy", "Nara", "Rowena", "Sabrina", "Scarlet",
            "Shelby", "Shirley", "Sparrow", "Spring", "Storm", "Summer",
            "Taylor", "Tina", "Trudy", "Ulla", "Verity", "Wendy", "Whitney",
            " Wilona"
        };

    // source:
    // http://en.wikipedia.org/wiki/List_of_most_common_surnames#United_States
    static final String familyNames[] = { // 100 names
            "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller",
            "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson",
            "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez",
            "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall",
            "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill",
            "Scott", "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter",
            "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell",
            "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez",
            "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy",
            "Bailey", "Rivera", "Cooper", "Richardson", "Cox", "Howard", "Ward",
            "Torres", "Peterson", "Gray", "Ramirez", "James", "Watson",
            "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes",
            "Ross", "Henderson", "Coleman", "Jenkins", "Perry", "Powell",
            "Long", "Patterson", "Hughes", "Flores", "Washington", "Butler",
            "Simmons", "Foster", "Gonzales", "Bryant", "Alexander", "Russell",
            "Griffin", "Diaz*", "Hayes*"
        };

    static List<Flag> served = new ArrayList<Flag>();
    static List<Flag> allIcons;

    static {
        getAllIcons();
    }

    public static enum Club {
        Alpha, Geeks, Phi, Kappa, Lions
    }

    /** Custom type. */
    public static class Tutor implements Comparable<Tutor> {
        String name, surname;

        Tutor() {
            name = surname = "";
        }

        Tutor(String firstName, String surname) {
        	this.surname = surname;
            this.name = firstName + " " + surname;
        }

        @Override public int hashCode() {
            return name.hashCode();
        }

        @Override public boolean equals(Object obj) {
            return (obj instanceof Tutor) && ((Tutor) obj).name.equals(name);
        }

        @Override public String toString() {
            return name;
        }

        @Override public int compareTo(Tutor o) {
            return (o == null) ? 1 : surname.compareTo(o.surname);
        }
    }

    public static class Flag extends ImageIcon implements Comparable<Flag>{
        private static final long serialVersionUID = 1242769439980562528L;
        private Double redAmount;
        private String fileLocation;

        Flag(String name, byte array[]) {
            super(array);
            fileLocation = "http://coderazzi.net/private/flags/"+name;
        }

        public double getRedAmount() {
            if (redAmount == null) {
                // Graphics g = getImage().getR();
                int w = getIconWidth();
                int h = getIconHeight();
                if ((w > 0) && (h > 0)) {
                    BufferedImage buffImage = new BufferedImage(w, h,
                            BufferedImage.TYPE_INT_ARGB);
                    buffImage.getGraphics()
                        .drawImage(this.getImage(), 0, 0, null);

                    int up = 0;
                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < h; j++) {
                            int c = buffImage.getRGB(i, j);
                            int red = (c & 0x00ff0000) >> 16;
                            int green = (c & 0x0000ff00) >> 8;
                            int blue = c & 0x000000ff;
                            if ((red > green) && (red > blue)) {
                                up++;
                            }
                        }
                    }

                    redAmount = ((double) up) / (h * w);
                }
            }

            return redAmount;
        }
        
        public String getFileLocation(){
        	return fileLocation;
        }

        @Override
        public int compareTo(Flag o) {
            return fileLocation.compareTo(o.fileLocation);
        }
    }

    public String name;
    public String firstName;
    public Integer age;
    public Boolean male;
    public Tutor tutor;
    public Flag flag;
    public Club club;
    public Date date;
    public String note;
    public String htmlFlag;

    public TestData() {
        // 1 out of 64 can be with unknown age (null)
        if (random.nextBoolean() || random.nextBoolean() || random
                .nextBoolean() || random.nextBoolean() || random
                .nextBoolean() || random.nextBoolean()) {
            age = 7 + random.nextInt(100);
        }

        male = random.nextBoolean();
        firstName = getFirstName(male);
        name = getName(firstName);
        // 1 out of 4 can be null
        if (random.nextBoolean() || random.nextBoolean()) {
            // 1 out of 4 can be empty
            if (random.nextBoolean() && random.nextBoolean()) {
                tutor = new Tutor();
            } else {
                tutor = new Tutor(getFirstName(random.nextBoolean()),
                        getSurname());
            }
        }
        // 1 out of 16 can be without flag
        if (random.nextBoolean() || random.nextBoolean() || random
                .nextBoolean() || random.nextBoolean()) {
            flag = getFlag();
        }

        club = getClub();
        date = new GregorianCalendar(random.nextInt(50) + 1940,
                random.nextInt(12), random.nextInt(28)).getTime();
        
        if (random.nextBoolean() && random.nextBoolean()) {
        	if (random.nextBoolean()) {
        		note = "<html><i>Transferral <font color='red'>"+
        				"not started</font></i></html>";
        	} else if (random.nextBoolean()){
        		note = "<html><i>Transferral &#34;started&#34;</i></html>";
        	} else if (random.nextBoolean()){
        		note = "> *";
        	} else if (random.nextBoolean()){
        		note = "=";
        	} else {        		
        		note = "<html><font color='red'>&gt; *</font></html>";
        	}
        }
        
        if (flag!=null){
        	htmlFlag = "<html><p style='padding: 0px 16px;'><img src=\'"+flag.getFileLocation()+"\'>";
        }
    }

    static void getAllIcons() {
        allIcons = new ArrayList<Flag>();
        try {
            Pattern p = Pattern.compile("gif/(.+)\\.gif");
            ZipInputStream zip = new ZipInputStream(TestData.class
                        .getResourceAsStream(
                            "/net/coderazzi/filters/examples/resources/famfamfam_flag_icons.zip"));
            ZipEntry entry;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buffer[] = new byte[16384];

            while ((entry = zip.getNextEntry()) != null) {
                Matcher m = p.matcher(entry.getName());

                if (m.matches()) {
                    baos.reset();

                    int read = 0;
                    while ((read = zip.read(buffer)) > 0) {
                        baos.write(buffer, 0, read);
                    }

                    Flag ic = new Flag(entry.getName(), baos.toByteArray());
                    ic.setDescription(m.group(1));
                    allIcons.add(ic);
                }

                zip.closeEntry();
            }

            zip.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Error reading icons:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void resetRandomness() {
        random = new Random();
        served.clear();
        getAllIcons();
    }

    private String getFirstName(boolean male) {
        String source[] = male ? maleNames : femaleNames;

        return source[random.nextInt(source.length)];
    }

    private String getName(String firstName) {
        return firstName + " " + getSurname();
    }

    private String getSurname() {

        return familyNames[random.nextInt(familyNames.length)];
    }

    private Club getClub() {
        Club clubs[] = Club.values();

        return clubs[random.nextInt(clubs.length)];
    }

    private Flag getFlag() {

        for (Flag ic : served) {

            if (random.nextBoolean()) {
                return ic;
            }
        }

        if (!allIcons.isEmpty()) {
            Flag ret = allIcons.remove(random.nextInt(allIcons.size()));
            served.add(ret);

            return ret;
        }

        if (!served.isEmpty()) {
            return served.get(random.nextInt(served.size()));
        }

        return null;
    }

}
