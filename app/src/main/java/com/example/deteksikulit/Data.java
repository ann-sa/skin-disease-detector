package com.example.deteksikulit;

public class Data {

    public static String name;

    public static String[] classNames = new String[]{
            "dermatitis_perioral", "karsinoma", "pustula", "rosacea", "tinea_facialis"};

    public static String[][] mainArray=new String[][]{{
            "Dermatitis Perioral",
            "Dermatitis perioral biasanya muncul sebagai ruam benjolan merah di sekitar mulut dan di lipatan sekitar hidung. " +
                    "Benjolan terkadang dapat muncul disertai dengan sisik, dan bisa juga terlihat pada area di bawah mata, dahi, dan dagu. " +
                    "Benjolan dengan ukuran kecil ini dapat berisi cairan atau nanah dan terlihat mirip dengan jerawat." + "\n\n" +
                    "Gejala lain yang muncul selain ruam adalah rasa gatal dan terbakar, terutama saat kondisi ruam mulai memburuk. " +
                    "Jika kondisi ruam sudah memburuk seperti penderita mengalami rasa gatal yang tidak tertahankan, " +
                    "sebisa mungkin hindari garukan, terlebih dengan kondisi tangan yang tidak bersih."
    },{
            "Karsinoma",
            "Karsinoma adalah jenis kanker yang tumbuh di jaringan kulit. " +
                    "Kondisi ini ditandai dengan perubahan yang terjadi pada kulit, seperti munculnya benjolan, bercak, " +
                    "atau tahi  lalat dengan bentuk dan ukuran yang tidak normal. " + "\n\n" +
                    "Kanker kulit disebabkan oleh perubahan atau mutase genetic pada sel kulit. " +
                    "Penyebab perubahan itu sendiri belum diketahui secara pasti, namun diduga akibat paparan sinar matahari yang berlebihan. " +
                    "Sinar ultraviolet dari matahari dapat merusak kulit dan memicu pertumbuhan yang tidak normal pada sel kulit."
    },{
            "Pustula",
            "Pustula adalah benjolan kecil di permukaan kulit yang berisi nanah, sehingga dikenal pula dengan sebutan jerawat nanah. " +
                    "Jerawat ini muncul sebagai benjolan yang ukurannya lebih besar dari komedo dengan puncak berwarna keputihan dan " +
                    "kulit sekitarnya berwarna kemerahan." + "\n\n" +
                    "Pada umumnya, pustula ini muncul di area wajah. Namun, bagian tubuh lainnya yang cenderung berminyak dapar diserang jerawat ini. " +
                    "Penyebab munculnya jerawat bernanah atau pustula adalah akibat dari penyumbatan pori-pori. " +
                    "Pori-pori yang seharusnya merupakan pintu keluar sebum (minyak) dan keringat menjadi tertutup akibat penumpukan sel kulit mati."
    },{
            "Rosacea",
            "Rosacea adalah penyakit kulit wajah yang ditandai dengan kulit kemerahan disertai bintik-bintik menyerupai jerawat. " +
                    "Kondisi ini juga dapat menyebabkan kulit wajah menebal dan pembuluh darah di wajah membengkak." + "\n\n" +
                    "Penyebab rosacea belum diketahui secara pasti, tetapi kondisi ini diduga terjadi karena faktor genetik dan lingkungan. " +
                    "Selain itu, ada beberapa factor yang diduga dapat memicu terjadinya rosacea seperti terpapar sinar matahari langsung, " +
                    "mengonsumsi minuman beralkohol, terjadi perubahan suhu udara dingin atau panas yang ekstrem, mengalami stres."
    },{
            "Tinea Facialis",
            "Tinea fasialis merupakan penyakit kulit pada wajah yang tampak sebagai bercak kemerahan dan bersisik disertai rasa gatal. " +
                    "Kondisi ini tidak hanya dapat menganggu penampilan, namun juga bisa semakin parah dan " +
                    "menular ke orang lain bila tidak segera ditangani." + "\n\n" +
                    "Tinea fasialis atau tinea faciei dapat diartikan sebagai infeksi jamur yang terjadi di kulit wajah dan " +
                    "bisa muncul pada pipi, dagu, bibir, dahi, atau pun di sekitar mata. " +
                    "Infeksi ini lebih mudah terjadi pada orang yang sering berkeringat atau kurang menjaga kebersihan kulitnya."
    }};

    public static String getName() {
        return name;
    }

    public static void setName(String name){
        Data.name=name;
    }
}