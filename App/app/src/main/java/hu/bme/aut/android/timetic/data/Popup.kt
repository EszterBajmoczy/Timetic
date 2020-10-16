package hu.bme.aut.android.timetic.data

import com.framgia.library.calendardayview.data.IPopup
import java.util.*

class Popup : IPopup {
    var startTime1: Calendar? = null
    var endTime1: Calendar? = null
    var imageStart1: String? = null
    var imageEnd1: String? = null
    var title1: String? = null
    var description1: String? = null
    var quote1: String? = null
    fun setTitle(title: String?) {
        this.title1 = title
    }

    override fun getTitle(): String? {
        return title1!!
    }

    fun setDescription(description: String?) {
        this.description1 = description
    }

    override fun getDescription(): String? {
        return description1!!
    }

    fun setQuote(quote: String?) {
        this.quote1 = quote
    }

    override fun getQuote(): String? {
        return quote1
    }

    fun setImageStart(imageStart: String?) {
        this.imageStart1 = imageStart
    }

    override fun getImageStart(): String? {
        return imageStart1!!
    }

    fun setImageEnd(imageEnd: String?) {
        this.imageEnd1 = imageEnd
    }

    override fun getImageEnd(): String? {
        return imageEnd1!!
    }

    override fun isAutohide(): Boolean {
        return false
    }

    fun setStartTime(startTime: Calendar?) {
        this.startTime1 = startTime
    }

    override fun getStartTime(): Calendar? {
        return startTime1!!
    }

    fun setEndTime(endTime: Calendar?) {
        this.endTime1 = endTime
    }

    override fun getEndTime(): Calendar? {
        return endTime1!!
    }
}