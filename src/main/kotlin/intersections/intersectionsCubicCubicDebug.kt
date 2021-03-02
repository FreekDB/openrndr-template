package intersections

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.math.YPolarity
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }

    val c0 = ShapeContour(
        segments = listOf(Segment(
            start = Vector2(
                x = 442.0490241658826,
                y = 938.8940621364709
            ),
            end = Vector2(x = 436.7881653233186, y = 936.8317197306876),
            control = arrayOf(Vector2(
                x = 440.4575796403639,
                y = 938.7531469890783
            ), Vector2(x = 438.61846498056343, y = 937.9748291373821))
        ), Segment(
            start = Vector2(
                x = 436.7881653233186,
                y = 936.8317197306876
            ),
            end = Vector2(x = 428.3519257501763, y = 928.9772853158902),
            control = arrayOf(Vector2(
                x = 433.580277963478,
                y = 934.908422540653
            ), Vector2(x = 430.3790718623975, y = 931.9490955148297))
        ), Segment(
            start = Vector2(
                x = 428.3519257501763,
                y = 928.9772853158902
            ),
            end = Vector2(
                x = 426.45635201885057,
                y = 925.0283503121417
            ),
            control = arrayOf(Vector2(
                x = 427.3966448972445,
                y = 927.6288832023056
            ), Vector2(x = 426.7101456300917, y = 926.2740100475478))
        ), Segment(
            start = Vector2(
                x = 426.45635201885057,
                y = 925.0283503121417
            ),
            end = Vector2(x = 426.2984456897422, y = 923.3397638073709),
            control = arrayOf(Vector2(
                x = 426.3421875740049,
                y = 924.5150474511403
            ), Vector2(x = 426.29278399710546, y = 923.9471162357422))
        ), Segment(
            start = Vector2(
                x = 426.2984456897422,
                y = 923.3397638073709
            ),
            end = Vector2(x = 427.6497541637639, y = 916.5627800961719),
            control = arrayOf(Vector2(
                x = 426.2891407413603,
                y = 921.3324891729327
            ), Vector2(x = 426.80038403608773, y = 918.9457379269165))
        ), Segment(
            start = Vector2(
                x = 427.6497541637639,
                y = 916.5627800961719
            ),
            end = Vector2(x = 433.3703388494651, y = 906.7517541215141),
            control = arrayOf(Vector2(
                x = 428.9348472804299,
                y = 912.8160608853766
            ), Vector2(x = 431.0362620553795, y = 909.0650506757398))
        ), Segment(
            start = Vector2(
                x = 433.3703388494651,
                y = 906.7517541215141
            ),
            end = Vector2(x = 435.3194971681168, y = 905.231313655894),
            control = arrayOf(Vector2(
                x = 434.0024478299536,
                y = 906.1031347059048
            ), Vector2(x = 434.6564224677324, y = 905.5799437838788))
        ), Segment(
            start = Vector2(
                x = 435.3194971681168,
                y = 905.231313655894
            ),
            end = Vector2(x = 440.3919343740061, y = 904.1270373708342),
            control = arrayOf(Vector2(
                x = 436.60194226635423,
                y = 904.5228510213844
            ), Vector2(x = 438.3935801257989, y = 904.1857845636355))
        ), Segment(
            start = Vector2(
                x = 440.3919343740061,
                y = 904.1270373708342
            ),
            end = Vector2(
                x = 441.46474658974756,
                y = 904.1038746912468
            ),
            control = arrayOf(Vector2(
                x = 440.74377689577193,
                y = 904.1112742822773
            ), Vector2(x = 441.1018184942657, y = 904.1036515387491))
        ), Segment(
            start = Vector2(
                x = 441.46474658974756,
                y = 904.1038746912468
            ),
            end = Vector2(x = 445.7571677943859, y = 904.4197614442479),
            control = arrayOf(Vector2(
                x = 442.84904080669264,
                y = 904.1018433849482
            ), Vector2(x = 444.3032507789102, y = 904.2119776972856))
        ), Segment(
            start = Vector2(
                x = 445.7571677943859,
                y = 904.4197614442479
            ),
            end = Vector2(
                x = 455.99480704561745,
                y = 907.8205499599337
            ),
            control = arrayOf(Vector2(
                x = 449.61929239089875,
                y = 904.9401959561083
            ), Vector2(x = 453.4884277473108, y = 906.1502892447677))
        ), Segment(
            start = Vector2(
                x = 455.99480704561745,
                y = 907.8205499599337
            ),
            end = Vector2(x = 457.25393769536, y = 908.8176020684253),
            control = arrayOf(Vector2(
                x = 456.4772108523003,
                y = 908.1328305748876
            ), Vector2(x = 456.90196452414494, y = 908.4653109737365))
        ), Segment(
            start = Vector2(
                x = 457.25393769536,
                y = 908.8176020684253
            ),
            end = Vector2(x = 460.0312893784585, y = 913.6704117124516),
            control = arrayOf(Vector2(
                x = 458.412673277219,
                y = 909.9382276010163
            ), Vector2(x = 459.34725454140835, y = 911.6770755807263))
        ), Segment(
            start = Vector2(
                x = 460.0312893784585,
                y = 913.6704117124516
            ),
            end = Vector2(x = 461.3167971320989, y = 919.0516139105351),
            control = arrayOf(Vector2(
                x = 460.63661400145264,
                y = 915.3471820156869
            ), Vector2(x = 461.07405539348787, y = 917.1992132185229))
        ), Segment(
            start = Vector2(
                x = 461.3167971320989,
                y = 919.0516139105351
            ),
            end = Vector2(x = 461.5509662635632, y = 922.4521026945348),
            control = arrayOf(Vector2(
                x = 461.47231684675313,
                y = 920.1987391446384
            ), Vector2(x = 461.5528223246434, y = 921.346117791953))
        ), Segment(
            start = Vector2(
                x = 461.5509662635632,
                y = 922.4521026945348
            ),
            end = Vector2(x = 460.6021172579933, y = 927.9578339432301),
            control = arrayOf(Vector2(
                x = 461.57704891276575,
                y = 924.5549864974
            ), Vector2(x = 461.2896177165878, y = 926.5048736465536))
        ), Segment(
            start = Vector2(
                x = 460.6021172579933,
                y = 927.9578339432301
            ),
            end = Vector2(x = 459.3928300312768, y = 929.8459810614091),
            control = arrayOf(Vector2(
                x = 460.32024868688694,
                y = 928.5806967169851
            ), Vector2(x = 459.9066080189868, y = 929.2146764319164))
        ), Segment(
            start = Vector2(
                x = 459.3928300312768,
                y = 929.8459810614091
            ),
            end = Vector2(x = 450.050626442244, y = 936.8371564475884),
            control = arrayOf(Vector2(
                x = 457.3059060863847,
                y = 932.4853994416618
            ), Vector2(x = 453.7141757320359, y = 935.1001225162677))
        ), Segment(
            start = Vector2(
                x = 450.050626442244,
                y = 936.8371564475884
            ),
            end = Vector2(x = 449.4685388353473, y = 937.1097281844613),
            control = arrayOf(Vector2(
                x = 449.85675683067177,
                y = 936.9305301467333
            ), Vector2(x = 449.6626587988286, y = 937.0214311276835))
        ), Segment(
            start = Vector2(
                x = 449.4685388353473,
                y = 937.1097281844613
            ),
            end = Vector2(
                x = 442.68830276940014,
                y = 938.9238061012122
            ),
            control = arrayOf(Vector2(
                x = 447.0701022910057,
                y = 938.2294612052816
            ), Vector2(x = 444.6640475898481, y = 938.9376171389744))
        ), Segment(
            start = Vector2(
                x = 442.68830276940014,
                y = 938.9238061012122
            ),
            end = Vector2(x = 442.0490241658826, y = 938.8940621364709),
            control = arrayOf(Vector2(
                x = 442.46860900948974,
                y = 938.9249317417105
            ), Vector2(x = 442.255202928489, y = 938.9153011560522))
        )), closed = true, polarity = YPolarity.CW_NEGATIVE_Y
    )
    val c1 = ShapeContour(
        segments = listOf(Segment(
            start = Vector2(
                x = 427.5412369967128,
                y = 926.6823710647726
            ),
            end = Vector2(x = 421.5842515382645, y = 924.9026757677475),
            control = arrayOf(Vector2(
                x = 425.92460284725297,
                y = 926.7027481353667
            ), Vector2(x = 423.82117980253804, y = 926.0087103713581))
        ), Segment(
            start = Vector2(
                x = 421.5842515382645,
                y = 924.9026757677475
            ),
            end = Vector2(
                x = 418.53651368110354,
                y = 923.2595714131518
            ),
            control = arrayOf(Vector2(
                x = 420.5852121036895,
                y = 924.4257141505461
            ), Vector2(x = 419.5606699903645, y = 923.8718056101063))
        ), Segment(
            start = Vector2(
                x = 418.53651368110354,
                y = 923.2595714131518
            ),
            end = Vector2(x = 409.5701516361747, y = 916.2531432363273),
            control = arrayOf(Vector2(
                x = 415.26689035119125,
                y = 921.3179044646978
            ), Vector2(x = 411.9993167932852, y = 918.7890617906511))
        ), Segment(
            start = Vector2(
                x = 409.5701516361747,
                y = 916.2531432363273
            ),
            end = Vector2(
                x = 406.13679902928425,
                y = 911.3222962394401
            ),
            control = arrayOf(Vector2(
                x = 407.8861240662935,
                y = 914.5476715498718
            ), Vector2(x = 406.6181764413719, y = 912.8348653959051))
        ), Segment(
            start = Vector2(
                x = 406.13679902928425,
                y = 911.3222962394401
            ),
            end = Vector2(x = 406.1133410355488, y = 911.2486250730934),
            control = arrayOf(Vector2(
                x = 406.1287015059883,
                y = 911.2976735604926
            ), Vector2(x = 406.12088084263377, y = 911.2731160119328))
        ), Segment(
            start = Vector2(
                x = 406.1133410355488,
                y = 911.2486250730934
            ),
            end = Vector2(x = 405.8030731379928, y = 908.9261972019758),
            control = arrayOf(Vector2(
                x = 405.88681295071876,
                y = 910.545533795904
            ), Vector2(x = 405.7918732111931, y = 909.7587017651352))
        ), Segment(
            start = Vector2(
                x = 405.8030731379928,
                y = 908.9261972019758
            ),
            end = Vector2(x = 406.4086104474991, y = 904.866916746137),
            control = arrayOf(Vector2(
                x = 405.79605838924556,
                y = 907.6391949022154
            ), Vector2(x = 406.0173059736781, y = 906.2520769719353))
        ), Segment(
            start = Vector2(
                x = 406.4086104474991,
                y = 904.866916746137
            ),
            end = Vector2(x = 410.4688334564247, y = 897.0517188999826),
            control = arrayOf(Vector2(
                x = 407.18524793761594,
                y = 901.9661145536048
            ), Vector2(x = 408.6815911099229, y = 899.0671740663101))
        ), Segment(
            start = Vector2(
                x = 410.4688334564247,
                y = 897.0517188999826
            ),
            end = Vector2(x = 411.8201162532799, y = 895.7407233770913),
            control = arrayOf(Vector2(
                x = 410.90179750287047,
                y = 896.5507964933736
            ), Vector2(x = 411.3543731164688, y = 896.1080746698059))
        ), Segment(
            start = Vector2(
                x = 411.8201162532799,
                y = 895.7407233770913
            ),
            end = Vector2(
                x = 419.50163571192405,
                y = 892.5395490837564
            ),
            control = arrayOf(Vector2(
                x = 413.53999916835437,
                y = 894.3520634330179
            ), Vector2(x = 416.3970651503617, y = 893.2218413101461))
        ), Segment(
            start = Vector2(
                x = 419.50163571192405,
                y = 892.5395490837564
            ),
            end = Vector2(x = 422.4423371045807, y = 892.0064538338512),
            control = arrayOf(Vector2(
                x = 420.46578160197777,
                y = 892.3175076930601
            ), Vector2(x = 421.4539925791263, y = 892.137698876724))
        ), Segment(
            start = Vector2(
                x = 422.4423371045807,
                y = 892.0064538338512
            ),
            end = Vector2(x = 426.158087442941, y = 891.7473404003069),
            control = arrayOf(Vector2(
                x = 423.69733499959796,
                y = 891.8365260859963
            ), Vector2(x = 424.95257797413745, y = 891.7454945745034))
        ), Segment(
            start = Vector2(
                x = 426.158087442941,
                y = 891.7473404003069
            ),
            end = Vector2(
                x = 431.80139836274356,
                y = 892.7383607007491
            ),
            control = arrayOf(Vector2(
                x = 428.3220406160789,
                y = 891.7189810923218
            ), Vector2(x = 430.3219210289131, y = 892.0066757725705))
        ), Segment(
            start = Vector2(
                x = 431.80139836274356,
                y = 892.7383607007491
            ),
            end = Vector2(
                x = 432.37091335129355,
                y = 893.0563199937714
            ),
            control = arrayOf(Vector2(
                x = 432.00362781697083,
                y = 892.8339020357824
            ), Vector2(x = 432.1940333220025, y = 892.9396403041567))
        ), Segment(
            start = Vector2(
                x = 432.37091335129355,
                y = 893.0563199937714
            ),
            end = Vector2(x = 435.198246034818, y = 896.3191997888123),
            control = arrayOf(Vector2(
                x = 433.3904218732497,
                y = 893.6999430900654
            ), Vector2(x = 434.3404007823737, y = 894.8621576943436))
        ), Segment(
            start = Vector2(
                x = 435.198246034818,
                y = 896.3191997888123
            ),
            end = Vector2(x = 439.1164972433261, y = 906.1884150468093),
            control = arrayOf(Vector2(
                x = 436.84762896823827,
                y = 898.9951604687135
            ), Vector2(x = 438.2241643938649, y = 902.5923131816958))
        ), Segment(
            start = Vector2(
                x = 439.1164972433261,
                y = 906.1884150468093
            ),
            end = Vector2(x = 440.2503098914884, y = 914.2543846009603),
            control = arrayOf(Vector2(
                x = 439.83800314928317,
                y = 909.0272849077203
            ), Vector2(x = 440.2554477860439, y = 911.8676891185869))
        ), Segment(
            start = Vector2(
                x = 440.2503098914884,
                y = 914.2543846009603
            ),
            end = Vector2(x = 439.830487899187, y = 917.6280554045525),
            control = arrayOf(Vector2(
                x = 440.26524136948643,
                y = 915.5625687872955
            ), Vector2(x = 440.1385626919736, y = 916.7207355602304))
        ), Segment(
            start = Vector2(
                x = 439.830487899187,
                y = 917.6280554045525
            ),
            end = Vector2(
                x = 439.62017950000654,
                y = 918.1816917041988
            ),
            control = arrayOf(Vector2(
                x = 439.77033508166664,
                y = 917.8125937488946
            ), Vector2(x = 439.69996785021317, y = 917.9972850484205))
        ), Segment(
            start = Vector2(
                x = 439.62017950000654,
                y = 918.1816917041988
            ),
            end = Vector2(
                x = 436.06368596676964,
                y = 922.7520730717011
            ),
            control = arrayOf(Vector2(
                x = 438.9474583008484,
                y = 919.7987085590024
            ), Vector2(x = 437.64873195000246, y = 921.3971273751845))
        ), Segment(
            start = Vector2(
                x = 436.06368596676964,
                y = 922.7520730717011
            ),
            end = Vector2(
                x = 431.06536273287037,
                y = 925.8859636797919
            ),
            control = arrayOf(Vector2(
                x = 434.57213827761274,
                y = 924.0754418697892
            ), Vector2(x = 432.82181493087546, y = 925.1858549845774))
        ), Segment(
            start = Vector2(
                x = 431.06536273287037,
                y = 925.8859636797919
            ),
            end = Vector2(x = 427.5412369967128, y = 926.6823710647726),
            control = arrayOf(Vector2(
                x = 429.8626519440937,
                y = 926.393685283067
            ), Vector2(x = 428.6544181210498, y = 926.690577264292))
        )), closed = true, polarity = YPolarity.CW_NEGATIVE_Y
    )

    var seg0 = 0
    var seg1 = 0

    val ints = c0.intersections(c1)
    println(ints.size)

    program {
        extend {
            drawer.fill = null
            drawer.translate(drawer.bounds.center-c0.bounds.center * 10.5)
            drawer.scale(10.5)

            drawer.stroke = ColorRGBa.YELLOW
            drawer.strokeWeight = 0.5
            drawer.contours(listOf(
                c0.segments[seg0].contour,
                c1.segments[seg1].contour
            ))

            drawer.strokeWeight = 0.1
            drawer.stroke = ColorRGBa.WHITE
            drawer.contour(c0)
            drawer.contour(c1)
            drawer.strokeWeight = 0.2
            drawer.circle(ints[0].position, 2.0)

            drawer.stroke = null
            drawer.fill = ColorRGBa.RED
            drawer.circles(c0.segments.map { it.start }, 0.5)
            drawer.circles(c1.segments.map { it.start }, 0.5)
        }
        keyboard.keyDown.listen { key ->
            val n0 = c0.segments.size
            val n1 = c1.segments.size
            when (key.name) {
                "arrow-up" -> seg0 = (seg0 + 1) % n0
                "arrow-down" -> seg0 = (seg0 - 1 + n0) % n0
                "arrow-right" -> seg1 = (seg1 + 1) % n1
                "arrow-left" -> seg1 = (seg1 - 1 + n1) % n1
                "escape" -> application.exit()
                "enter" -> {
                    println(c0.segments[seg0])
                    println(c1.segments[seg1])
                    c0.segments[seg0].intersections(c1.segments[seg1]).forEach {
                        println("${it.position}")
                    }
                }
            }
        }
    }
}
