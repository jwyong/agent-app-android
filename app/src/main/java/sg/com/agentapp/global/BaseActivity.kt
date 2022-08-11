package sg.com.agentapp.global

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import sg.com.agentapp.R


/* Created by Soapp on 4 July 2017. */
open class BaseActivity : AppCompatActivity() {
    protected fun setupToolbar() {
        val mToolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // setup gradient bg if need
    protected fun setGradientBG(whiteSize: Int) {
        UIHelper().gradientBgWhiteBig(this, findViewById(R.id.gradient_bg), whiteSize)
    }
}
