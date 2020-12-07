package hu.bme.aut.android.timetic

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import hu.bme.aut.android.timetic.R

class CurrentDayDecorator(context: Activity?, currentDay: CalendarDay) : DayViewDecorator {
    private val drawable: Drawable? = ContextCompat.getDrawable(context!!, R.drawable.circle_background)
    var myDay = currentDay
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == myDay
    }

    override fun decorate(view: DayViewFacade) {
        //view.addSpan(DotSpan(5F, R.color.eventColor))
        if (drawable != null) {
            view.setBackgroundDrawable(drawable)
        }
    }
}