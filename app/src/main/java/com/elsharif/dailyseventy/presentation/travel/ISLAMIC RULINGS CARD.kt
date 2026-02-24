package com.elsharif.dailyseventy.presentation.travel


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ══════════════════════════════════════════════════════════════════════════════
//  ISLAMIC RULINGS CARD  –  أحكام المسافر (المذهب الشافعي)
//  مستخلص من الدرسين ٣٩ و٤٠ – عمدة السالك وعدة الناسك
//  معهد الإمام النووي للتفقه الشافعي
// ══════════════════════════════════════════════════════════════════════════════

// ─── Data ────────────────────────────────────────────────────────────────────

private data class IslamicRuling(
    val id     : String,
    val icon   : String,
    val title  : String,
    val summary: String,
    val color  : Color,
    val details: List<RulingDetail>
)

private data class RulingDetail(
    val label : String,
    val detail: String,
    val isNote: Boolean = false
)

private val TRAVELER_RULINGS = listOf(

    // ─────────────────────────────────────────────────────────────────
    // ١. شروط جواز القصر (٨ شروط)
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "qasr_conditions",
        icon    = "📏",
        title   = "شروط جواز القصر",
        summary = "٨ شروط يجب توافرها",
        color   = Color(0xFF1565C0),
        details = listOf(
            RulingDetail(
                "١ – الصلاة رباعية مؤداة",
                "يُقصَر الظهر والعصر والعشاء فقط (من ٤ إلى ٢). الفجر والمغرب لا يُقصَران. " +
                        "يُشترط أن تكون مؤداةً لا مقضية: فلو قضى في السفر صلاةً فاتته في الحضر أتمّها، " +
                        "ولو قضى في الحضر ما فاته في السفر أتمّه أيضاً."
            ),
            RulingDetail(
                "٢ – السفر الطويل (≥ ٨٢ كم)",
                "مرحلتان فأكثر = حوالي ٨٢ كيلومتراً بسير الأثقال المحمّلة مسيرة يوم وليلة. " +
                        "العبرة بقطع المسافة لا بالزمن، فلو قطعها بطائرة أو سفينة في لحظة جاز القصر."
            ),
            RulingDetail(
                "٣ – السفر مباح (غير معصية)",
                "الواجب والمندوب والمباح والمكروه: يجوز فيها القصر. أما الحرام فلا قصر. " +
                        "لو تاب العاصي في أثناء سفره وبقي أمامه مرحلتان فأكثر جاز له القصر، وإلا فلا."
            ),
            RulingDetail(
                "٤ – قصد مكان معلوم",
                "يجب أن يعلم أن مسافته مرحلتان فأكثر. الهائم لا يعرف وجهته لا يقصر. " +
                        "لو طلب عبداً آبقاً لا يعرف موضعه لم يجز إلا إذا علم أنه لا يجده إلا بعد مرحلتين."
            ),
            RulingDetail(
                "٥ – مجاوزة عمران البلد",
                "إن كان للبلد سور → قصر بمجرد مجاوزة السور (حتى وإن وُجد عمران خارجه). " +
                        "إن لم يكن للبلد سور → يجب مجاوزة العمران كله حتى وإن تخلّله خراب. " +
                        "المزارع والبساتين والمقابر وراء البلد لا يجب مجاوزتها. " +
                        "البدوي يبدأ سفره بمفارقة خيام قومه لا خيمته وحدها."
            ),
            RulingDetail(
                "٦ – دوام السفر إلى تمام الصلاة",
                "لو نوى الإقامة في أثناء الصلاة أو أقام فعلاً وجب الإتمام. " +
                        "نية إقامة ٤ أيام كاملة غير يومَي الدخول والخروج تُسقط الرخصة من لحظة النية."
            ),
            RulingDetail(
                "٧ – نية القصر في تكبيرة الإحرام",
                "يجب اقتران نية القصر بتكبيرة الإحرام يقيناً. لو تأخرت أو تقدّمت وجب الإتمام. " +
                        "(المزني: يجوز النية في أثناء الصلاة. أبو حنيفة: القصر عزيمة لا تحتاج نية)."
            ),
            RulingDetail(
                "٨ – ألّا يقتدي بمتمّ في جزء من صلاته",
                "لو ائتمّ المسافر بمقيم في أي جزء من صلاته ولو في التشهد الأخير أو السلام وجب الإتمام. " +
                        "لو جهل نية إمامه وعلّق نيته عليها صحّ: إن قصر الإمام قصر، وإن أتمّ أتمّ."
            ),
            RulingDetail(
                "⚠️ حالات يجب فيها الإتمام",
                "• نوى الإقامة في أثناء الصلاة\n" +
                        "• شكّ هل نوى القصر أم لا\n" +
                        "• تردّد هل يُتمّ أم يقصر\n" +
                        "• فرّ من سفره في أثناء الصلاة",
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٢. القصر أم الإتمام؟
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "qasr_better",
        icon    = "⚖️",
        title   = "القصر أم الإتمام؟",
        summary = "الأصل، والأفضل، والواجب",
        color   = Color(0xFF2E7D32),
        details = listOf(
            RulingDetail(
                "الأصل: الإتمام أفضل",
                "في غير الحالات المذكورة الإتمام أفضل للمسافر؛ خروجاً من الخلاف. " +
                        "مثاله: الملاح الذي معه أهله في سفينته، الإمام مالك لا يُجيز له القصر فالأولى له الإتمام."
            ),
            RulingDetail(
                "القصر أفضل في ٤ حالات",
                "١) إذا بلغت المسافة ≥ ١٢٣ كم (٣ مراحل)؛ خروجاً من خلاف أبي حنيفة الذي أوجبه.\n" +
                        "٢) إذا وجد في نفسه كراهية القصر.\n" +
                        "٣) إذا شكّ في دليل جواز القصر.\n" +
                        "٤) إذا كان ممّن يُقتدى به أمام الناس."
            ),
            RulingDetail(
                "القصر واجب في حالة واحدة",
                "إذا ضاق الوقت عن الإتمام في حال السفر وجب القصر؛ " +
                        "حتى لا يخرج جزء من الصلاة عن الوقت."
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٣. متى ينتهي السفر؟
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "end_of_travel",
        icon    = "🏁",
        title   = "متى ينتهي السفر؟",
        summary = "٣ صور لانقطاع رخصة السفر",
        color   = Color(0xFFC62828),
        details = listOf(
            RulingDetail(
                "الصورة الأولى: الوصول إلى الوطن",
                "بمجرد وصوله إلى سور وطنه (إن كان مسوّراً) أو إلى عمرانه (إن لم يكن مسوّراً) " +
                        "انقطع سفره حتى وإن لم يدخله."
            ),
            RulingDetail(
                "الصورة الثانية: نية إقامة ٤ أيام",
                "نية إقامة ٤ أيام كاملة بلياليها غير يومَي الدخول والخروج تُصيّره مقيماً من لحظة النية. " +
                        "دليله: «يمكث المهاجر بعد قضاء نُسكه ثلاثاً» فثلاثة أيام لا تُصيّره مقيماً وما فوقها يُصيّره. " +
                        "تثبت الإقامة في كل مكان حتى الصحراء."
            ),
            RulingDetail(
                "الصورة الثالثة: الإقامة الفعلية ٤ أيام",
                "إذا أقام فعلاً ٤ أيام غير يومَي الدخول والخروج وجب الإتمام حتى وإن لم ينوِ الإقامة؛ " +
                        "لأن الإقامة الفعلية أولى من النية."
            ),
            RulingDetail(
                "استثناء: المقيم لحاجة ينتظر إنجازها",
                "إذا أقام لحاجة يتوقع إنجازها في كل وقت وينوي الارتحال متى انقضت، " +
                        "جاز له القصر إلى ١٨ يوماً كاملة. " +
                        "دليله: إقامة النبي ﷺ في حرب هوازن ١٨ يوماً يقصر ينتظر انجلاء الحرب. " +
                        "فإن تأخّرت حاجته أكثر من ذلك أتمّ."
            ),
            RulingDetail(
                "📌 قاعدة يومَي الدخول والخروج",
                "• يوم الدخول ويوم الخروج لا يُحسبان من الأيام الأربعة.\n" +
                        "• لو دخل ليلاً لم يُحسب بقية الليل ويُحسب اليوم الذي يليه.\n" +
                        "• أبو حنيفة: ١٥ يوماً (مع يومَي الدخول والخروج) يُوجب الإتمام.",
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٤. أقسام العصاة في السفر
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "usat",
        icon    = "⚠️",
        title   = "أقسام العصاة في السفر",
        summary = "٣ أقسام وأحكامها",
        color   = Color(0xFFE65100),
        details = listOf(
            RulingDetail(
                "القسم الأول: العاصي بالسفر",
                "أنشأ سفره معصيةً من أوله (مثل: سافر ليسرق أو ليشرب الخمر). " +
                        "لا يجوز له القصر. لو تاب وبقي أمامه مرحلتان فأكثر جاز القصر، وإلا فلا."
            ),
            RulingDetail(
                "القسم الثاني: قلب سفره معصية",
                "أنشأ سفره لغرض مباح ثم قلبه معصية. لو تاب جاز له القصر مطلقاً " +
                        "بغض النظر عن المسافة المتبقية؛ لأن نشأة سفره كانت مباحة."
            ),
            RulingDetail(
                "القسم الثالث: العاصي في السفر",
                "أنشأ سفره مباحاً ووقعت منه معصية عارضة في أثنائه. " +
                        "لا يُمنع من القصر؛ لأنه لا يخلو أحد من الذنب."
            ),
            RulingDetail(
                "📌 رأي المخالفين",
                "أبو حنيفة والثوري والأوزاعي والمزني: يجوز للعاصي الترخص بجميع رخص السفر. " +
                        "المعتمد في المذهب الشافعي: لا رخصة في سفر المعصية.",
                isNote = true
            )
        )
    ),

/*
    // ─────────────────────────────────────────────────────────────────
    // ٥. الجمع – عامة
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "jam3_general",
        icon    = "🔗",
        title   = "الجمع بين الصلاتين",
        summary = "ما يجوز جمعه وأسبابه",
        color   = Color(0xFF1565C0),
        details = listOf(
            RulingDetail(
                "ما يجوز جمعه",
                "الظهر مع العصر، والمغرب مع العشاء. " +
                        "الجمعة كالظهر في جمع التقديم (نقله الزركشي واعتمده). " +
                        "لا تُجمع الفجر مع أي صلاة. " +
                        "لا يجوز جمع الجمعة مع العصر تأخيراً؛ لأن الجمعة لا تتأخر عن وقتها."
            ),
            RulingDetail(
                "أسباب الجمع (٣ أسباب)",
                "١) السفر الطويل: تقديماً وتأخيراً.\n" +
                        "٢) المطر: تقديماً فقط (على المعتمد).\n" +
                        "٣) المرض: اختاره الإمام النووي وجماعة وليس معتمد المذهب. " +
                        "ضابطه: مشقة شديدة إذا صلّى كل صلاة في وقتها."
            ),
            RulingDetail(
                "الجمع أم تركه؟",
                "الأفضل ترك الجمع؛ خروجاً من خلاف من لم يُجزه كأبي حنيفة.\n" +
                        "إلا في ٤ حالات يكون الجمع أفضل:\n" +
                        "١) في الحج (عرفة: تقديم، مزدلفة: تأخير).\n" +
                        "٢) من شكّ في دليل جواز الجمع.\n" +
                        "٣) من وجد في نفسه كراهية الجمع.\n" +
                        "٤) من يُقتدى به أمام الناس."
            )
        )
    ),
*/

    // ─────────────────────────────────────────────────────────────────
    // ٦. شروط جمع التقديم
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "taqdeem",
        icon    = "⬆️",
        title   = "شروط جمع التقديم",
        summary = "٤ شروط + محترزات",
        color   = Color(0xFF6A1B9A),
        details = listOf(
            RulingDetail(
                "الشرط الأول: الترتيب",
                "يبدأ بالأولى: الظهر قبل العصر، المغرب قبل العشاء. " +
                        "لو عكس: صحّت الأولى دون الثانية، وتقع الثانية نفلاً مطلقاً للجاهل أو الناسي."
            ),
            RulingDetail(
                "الشرط الثاني: نية الجمع قبل فراغ الأولى",
                "ينوي جمع التقديم في أثناء الصلاة الأولى ولو مع السلام منها. " +
                        "الأفضل أن تكون النية مع تكبيرة الإحرام. " +
                        "(المزني: لا تُشترط النية. معتمد المذهب: تُشترط).\n" +
                        "الفرق عن القصر: في القصر لا بد أن تقترن النية بالإحرام؛ لأن تأخيرها يُؤدّي جزءاً منها تاماً."
            ),
            RulingDetail(
                "الشرط الثالث: الموالاة",
                "لا يطول الفصل بين الصلاتين عرفاً. الضابط: أقل من ركعتين خفيفتين عند بعضهم. " +
                        "يُغتفر للمتيمم طلب خفيف؛ لأنه يحتاج إلى تيمم جديد للثانية."
            ),
            RulingDetail(
                "الشرط الرابع: دوام السفر إلى تمام الإحرام بالثانية",
                "يستمر عذر السفر إلى نهاية تكبيرة الإحرام بالصلاة الثانية. " +
                        "لو أقام أو نوى الإقامة قبل ذلك لم يجز الجمع."
            ),
            RulingDetail(
                "محترزات: متى تبطل أو يجب التأخير؟",
                "• قدّم الثانية على الأولى → الثانية باطلة.\n" +
                        "• لم ينوِ الجمع في الأولى → يجب تأخير الثانية إلى وقتها.\n" +
                        "• فرّق بينهما تفريقاً كثيراً → يجب تأخير الثانية إلى وقتها.\n" +
                        "• أقام قبل الشروع في الثانية → يجب تأخيرها.\n" +
                        "• أقام بعد فراغهما أو في أثناء الثانية → مضتا على الصحة."
            ),
            RulingDetail(
                "📌 الرواتب مع جمع التقديم",
                "سنة الظهر القبلية، ثم الفرضان، ثم سنة الظهر البعدية، ثم سنة العصر. " +
                        "لا يجوز تقديم راتبة الثانية قبل الفرضين.",
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٧. شروط جمع التأخير
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "takheer",
        icon    = "⬇️",
        title   = "شروط جمع التأخير",
        summary = "شرطان لا ثالث لهما",
        color   = Color(0xFF00695C),
        details = listOf(
            RulingDetail(
                "الشرط الأول: نية التأخير قبل خروج وقت الأولى",
                "ينوي تأخيرها إلى وقت الثانية قبل أن يخرج وقت الأولى. " +
                        "الرملي والخطيب: يمتد وقت النية حتى يبقى ما يسع الصلاة كاملةً. " +
                        "ابن حجر: يكفي أن يبقى ما يسع ركعة واحدة. " +
                        "لو أخّر الأولى بلا نية جمع تأخير → أثم وكانت قضاءً."
            ),
            RulingDetail(
                "الشرط الثاني: دوام السفر إلى تمام الصلاتين",
                "يستمر السفر إلى نهاية الصلاة الثانية. " +
                        "لو أقام قبل فعلهما بطل الجمع وصارت التابعة قضاءً لا إثم فيه."
            ),
            RulingDetail(
                "الفارق عن التقديم: الترتيب والموالاة مندوبان",
                "في جمع التأخير:\n" +
                        "• الترتيب (الأولى قبل الثانية): مندوب لا واجب.\n" +
                        "• الموالاة بين الصلاتين: مندوبة لا واجبة.\n" +
                        "• نية الجمع في الأولى: مندوبة."
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٨. مقارنة التقديم والتأخير
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "diff",
        icon    = "↔️",
        title   = "التقديم vs التأخير",
        summary = "مقارنة شاملة + الأفضلية",
        color   = Color(0xFF37474F),
        details = listOf(
            RulingDetail(
                "وقت النية",
                "التقديم: من الإحرام إلى السلام من الأولى.\n" +
                        "التأخير: من دخول وقت الأولى إلى أن يبقى ما يسع الصلاة."
            ),
            RulingDetail(
                "دوام العذر",
                "التقديم: إلى تمام الإحرام بالثانية.\n" +
                        "التأخير: إلى تمام الصلاة الثانية كاملةً."
            ),
            RulingDetail(
                "الموالاة والترتيب",
                "التقديم: واجبان.\n" +
                        "التأخير: مندوبان."
            ),
            RulingDetail(
                "أيهما أفضل للمسافر؟",
                "• سائر في وقت الأولى ونازل في وقت الثانية → التأخير أفضل (أيسر عليه).\n" +
                        "• نازل في وقت الأولى وسائر في وقت الثانية → التقديم أفضل.\n" +
                        "• نازل في وقتهما أو سائر فيهما → خلاف:\n" +
                        "  ابن حجر: التقديم أفضل (براءة ذمة).\n" +
                        "  الرملي: التأخير أفضل (وقت الثانية وقت للأولى)."
            )
        )
    ),

/*
    // ─────────────────────────────────────────────────────────────────
    // ٩. الجمع بعذر المطر
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "matar",
        icon    = "🌧️",
        title   = "الجمع بعذر المطر",
        summary = "تقديم فقط + ٤ مواضع للمطر",
        color   = Color(0xFF1565C0),
        details = listOf(
            RulingDetail(
                "تقديم فقط لا تأخير",
                "لا يجوز الجمع بالمطر تأخيراً؛ لأن المطر قد ينقطع قبل وقت الثانية " +
                        "فيؤدي إلى إخراج الأولى عن وقتها بلا عذر."
            ),
            RulingDetail(
                "وجود المطر يقيناً في ٤ مواضع",
                "١) عند الإحرام بالأولى.\n" +
                        "٢) عند السلام من الأولى.\n" +
                        "٣) بين سلام الأولى والتحرم بالثانية.\n" +
                        "٤) عند التحرم بالثانية.\n" +
                        "(لو انقطع بعد التحرم بالثانية لا يضر)."
            ),
            RulingDetail(
                "شرط الجماعة والمسافة",
                "يُشترط قصد جماعة في مكان بعيد عرفاً عن داره. " +
                        "لا يجوز الجمع لمن صلّى في داره أو قريباً منها. " +
                        "يكفي وجود الجماعة عند التحرم ولو انفرد بعدها."
            ),
            RulingDetail(
                "ضابط المطر المُجيز",
                "أن يحصل بلل أعلى الثوب وأسفل النعل. " +
                        "لا يجوز لمن مشى تحت مظلة أو في كنّ. " +
                        "لا يُشترط قوة المطر بل المدار على حصول البلل."
            ),
            RulingDetail(
                "ما يلحق بالمطر",
                "• نزول الماء من الميازيب بعد انقطاع المطر.\n" +
                        "• الثلج إذا كان قطعاً كباراً يحصل به التأذي.\n" +
                        "• البرد إذا كان ذائباً يبل الثوب."
            )
        )
    ),
*/

    // ─────────────────────────────────────────────────────────────────
    // ١٠. فطر رمضان
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "fitr",
        icon    = "🌙",
        title   = "فطر رمضان للمسافر",
        summary = "الحكم والشروط والقضاء",
        color   = Color(0xFF6A1B9A),
        details = listOf(
            RulingDetail(
                "يجوز الفطر بشروط",
                "يجوز الفطر إذا بلغت المسافة الحدّ الشرعي وخرج من بلده قبل طلوع الفجر. " +
                        "الصيام أفضل إن لم يَشقّ عليه."
            ),
            RulingDetail(
                "القضاء والكفارة",
                "يجب القضاء بعدد الأيام التي أفطرها. لا كفارة على المسافر الذي أفطر."
            ),
            RulingDetail(
                "لو شرع في الصيام ثم سافر",
                "جاز له الفطر في ذلك اليوم."
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ١١. المسح على الخفين
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "mash",
        icon    = "💧",
        title   = "المسح على الخفين",
        summary = "٣ أيام للمسافر، يوم للمقيم",
        color   = Color(0xFF00695C),
        details = listOf(
            RulingDetail(
                "مدة المسح",
                "المسافر: ٣ أيام بلياليها (٧٢ ساعة).\n" +
                        "المقيم: يوم وليلة (٢٤ ساعة)."
            ),
            RulingDetail(
                "بداية الحساب وشروط اللبس",
                "تُحسب المدة من أول مسح بعد الحدث لا من وقت اللبس. " +
                        "يُشترط لبس الخف على طهارة كاملة."
            ),
            RulingDetail(
                "نواقض المسح",
                "الجنابة، أو نزع الخف، أو انتهاء المدة."
            )
        )
    ),

    /*// ─────────────────────────────────────────────────────────────────
    // ١٢. رخص السفر القصير (المشتركة)
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id      = "short_rukhsa",
        icon    = "🛤️",
        title   = "رخص السفر القصير",
        summary = "٧ رخص تجوز في القصير والطويل",
        color   = Color(0xFF37474F),
        details = listOf(
            RulingDetail(
                "الرخص السبع المشتركة",
                "١) أكل الميتة للمضطر.\n" +
                        "٢) التنفل على الراحلة.\n" +
                        "٣) إسقاط الصلاة بالتيمم.\n" +
                        "٤) ترك الجمعة.\n" +
                        "٥) عدم القضاء لضرّات زوجة أخذت بالقرعة مدة السفر.\n" +
                        "٦) السفر بالوديعة لعذر.\n" +
                        "٧) السفر بالعارية."
            )
        )
    )*/
)

// ══════════════════════════════════════════════════════════════════════════════
//  COMPOSABLES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun IslamicRulingsCard() {
    var expandedSection by remember { mutableStateOf<String?>(null) }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {

            // ── Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(0.12f), CircleShape),
                    Alignment.Center
                ) { Text("📚", fontSize = 20.sp) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "أحكام المسافر",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "المذهب الشافعي – عمدة السالك",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // ── Rulings list
            TRAVELER_RULINGS.forEachIndexed { index, ruling ->
                RulingItem(
                    ruling   = ruling,
                    expanded = expandedSection == ruling.id,
                    onToggle = {
                        expandedSection =
                            if (expandedSection == ruling.id) null else ruling.id
                    }
                )
                if (index < TRAVELER_RULINGS.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun RulingItem(
    ruling  : IslamicRuling,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        // ── Row header
        Surface(
            onClick  = onToggle,
            modifier = Modifier.fillMaxWidth(),
            color    = Color.Transparent
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(ruling.color.copy(0.10f), RoundedCornerShape(10.dp)),
                    Alignment.Center
                ) { Text(ruling.icon, fontSize = 18.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        ruling.title,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = ruling.color
                    )
                    Text(
                        ruling.summary,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint     = ruling.color.copy(0.7f)
                )
            }
        }

        // ── Expanded body
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                ruling.details.forEach { item ->
                    if (item.isNote) {
                        // Note box (different styling)
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = ruling.color.copy(0.06f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    item.label,
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = ruling.color
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    item.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Normal detail item (expandable)
                        ExpandableDetailItem(item = item, accentColor = ruling.color)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExpandableDetailItem(item: RulingDetail, accentColor: Color) {
    var open by remember { mutableStateOf(false) }

    Surface(
        onClick  = { open = !open },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(if (open) 0.09f else 0.04f)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.label,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = accentColor,
                    modifier   = Modifier.weight(1f)
                )
                Icon(
                    if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint     = accentColor.copy(0.6f)
                )
            }

            AnimatedVisibility(
                visible = open,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = accentColor.copy(0.15f))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        item.detail,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}