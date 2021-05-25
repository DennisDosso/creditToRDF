package test.database;

public class CharacterReplacementTest {


    public static void main(String[] args) {

        String stringa_incriminata = "http://dbpedia.org/resource/\uF8FF_Inc";
        stringa_incriminata = "http://dbpedia.org/*resource/\uE347";
        System.out.println(stringa_incriminata);
//        stringa_incriminata = stringa_incriminata.replace("�", "'");
//        stringa_incriminata = stringa_incriminata.replaceAll("\uFFFD", "?");
//        stringa_incriminata = stringa_incriminata.replaceAll("[\uF000-\uF8FF]", "?");
        stringa_incriminata = stringa_incriminata.replaceAll("\\p{C}", "?");
        stringa_incriminata = stringa_incriminata.replaceAll("\\p{So}+", "?");
        stringa_incriminata = stringa_incriminata.replaceAll("\\*", "");
//        stringa_incriminata.replaceAll("[\uE000-\uF8FF]", "?");


        //U+E000–U+F8FF

        System.out.println(stringa_incriminata);
        System.out.println("haha");
    }
}
