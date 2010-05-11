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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class TestData {

    private static Random random = new Random();

    static final String[] maleNames = {
            "Alfred", "Alvin", "Blake", "Bob", "Brandon", "Bud", "Burton", "Charles", "Clark",
            "Dale", "Damon", "Darren", "Dustin", "Edward", "Elton", "Fletcher", "Forrester", "Gary",
            "Harley", "Harold", "Hugh", "James", "Keane", "Kenneth", "Landon", "Lee", "Lincoln",
            "Maxwell", "Miller", "Nash", "Nelson", "Norman", "Oswald", "Perry", "Prentice", "Ralph",
            "Raymond", "Richard", "Robert", "Scott", "Spencer", "Stanley", "Sutton", "Taylor",
            "Thorne", "Truman", "Tyler", "Wallace",
        };

    static final String[] femaleNames = {
            "Aida", "Ashley", "Audrey", "Beverly", "Brenda", "Brook", "Cameron", "Carling",
            "Chelsea", "Dale", "Dawn", "Devon", "Dustin", "Erika", "Farrah", "Harmony", "Hazel",
            "Heather", "Holly", "Jamie", "Joyce", "Joy", "Kim", "Kirsten", "Kyla", "Lark", "Lee",
            "Leigh", "Leslie", "Lindsay", "Mercy", "Nara", "Rowena", "Sabrina", "Scarlet", "Shelby",
            "Shirley", "Sparrow", "Spring", "Storm", "Summer", "Taylor", "Tina", "Trudy", "Ulla",
            "Verity", "Wendy", "Whitney", "Wilona"
        };

    // source: http://en.wikipedia.org/wiki/List_of_most_common_surnames#United_States
    static final String[] familyNames = {
            "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore",
            "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson",
            "Garcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker",
            "Hall", "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott",
            "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez",
            "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins",
            "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy",
            "Bailey", "Rivera", "Cooper", "Richardson", "Cox", "Howard", "Ward", "Torres",
            "Peterson", "Gray", "Ramirez", "James", "Watson", "Brooks", "Kelly", "Sanders", "Price",
            "Bennett", "Wood", "Barnes", "Ross", "Henderson", "Coleman", "Jenkins", "Perry",
            "Powell", "Long", "Patterson", "Hughes", "Flores", "Washington", "Butler", "Simmons",
            "Foster", "Gonzales", "Bryant", "Alexander", "Russell", "Griffin", "Diaz", "Hayes"
        };

    static List<Icon> served = new ArrayList<Icon>(), allIcons;

    static {
        getAllIcons();
    }

    public static enum Club {
        Alpha,
        Geeks,
        Kappa,
        Lions,
        Phi
    }

    public String name, firstName;
    public Integer age;
    public Boolean male;
    public String tutor;
    public Icon flag;
    public Club club;
    public Date date;

    public TestData() {
        age = 17 + random.nextInt(random.nextBoolean() ? 10 : 25);
        male = random.nextBoolean();
        firstName = getFirstName(male);
        name = getName(firstName);
        while ((tutor == null) || tutor.equals(name)) {
            tutor = (random.nextBoolean() || random.nextBoolean() || random.nextBoolean()) ? getName(random.nextBoolean()) : "";
        }
        flag = getFlag();
        club = getClub();
        // date is not exact (not everybody can be born at 00:00!)
        date = new GregorianCalendar(random.nextInt(50) + 1940, random.nextInt(12), random.nextInt(28), 1, 1).getTime();
    }

    static void getAllIcons() {
        allIcons = new ArrayList<Icon>();
        try {
            Pattern p = Pattern.compile("gif/(.+)\\.gif");
            ZipInputStream zip = new ZipInputStream(TestData.class.getResourceAsStream(
                        "/net/coderazzi/filters/examples/utils/famfamfam_flag_icons.zip"));
            ZipEntry entry;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];

            while ((entry = zip.getNextEntry()) != null) {
                Matcher m = p.matcher(entry.getName());

                if (m.matches()) {
                    baos.reset();

                    int read = 0;
                    while ((read = zip.read(buffer)) > 0) {
                        baos.write(buffer, 0, read);
                    }

                    ImageIcon ic = new ImageIcon(baos.toByteArray());
                    ic.setDescription(m.group(1));
                    allIcons.add(ic);
                }
                zip.closeEntry();
            }

            zip.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error reading icons:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void resetRandomness() {
        random = new Random();
        served.clear();
        getAllIcons();
    }

    private String getFirstName(boolean male) {
        String[] source = male ? maleNames : femaleNames;

        return source[random.nextInt(source.length - 1)];
    }

    private String getName(String firstName) {
        return firstName + " " + familyNames[random.nextInt(familyNames.length - 1)];
    }

    private String getName(boolean male) {
        String[] source = male ? maleNames : femaleNames;

        return source[random.nextInt(source.length - 1)] + " "
            + familyNames[random.nextInt(familyNames.length - 1)];
    }

    private Club getClub() {
        Club[] clubs = Club.values();

        return clubs[random.nextInt(clubs.length)];
    }

    private Icon getFlag() {

        for (Icon ic : served) {

            if (random.nextBoolean())
                return ic;
        }

        if (!allIcons.isEmpty()) {
            Icon ret = allIcons.remove(random.nextInt(allIcons.size()));
            served.add(ret);

            return ret;
        }

        if (!served.isEmpty()) {
            return served.get(random.nextInt(served.size()));
        }

        return null;
    }

}