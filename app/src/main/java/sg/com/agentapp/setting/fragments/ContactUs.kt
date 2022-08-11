package sg.com.agentapp.setting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment

import sg.com.agentapp.R
import sg.com.agentapp.global.IOBackPress

class ContactUs : Fragment(), IOBackPress {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.contact_us, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get the spinner from the xml.
        val dropdown = view.findViewById<Spinner>(R.id.spinner1)
        //create a list of items for the spinner.
        val items = arrayOf("Complaint", "Feedback", "Support")
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        val adapter = ArrayAdapter(this.context!!, android.R.layout.simple_spinner_dropdown_item, items)
        //set the spinners adapter to the previously created one.
        dropdown.adapter = adapter

    }

    override fun onBackPressed() {
        fragmentManager!!.popBackStack()
    }
}
