package sg.com.agentapp.global

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import sg.com.agentapp.R
import sg.com.agentapp.databinding.ChatMainBinding

class ImgPreview : BaseActivity() {
    private lateinit var binding: ChatMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.chat_main)

    }
}