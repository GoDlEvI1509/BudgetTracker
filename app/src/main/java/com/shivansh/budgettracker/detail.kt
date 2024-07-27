package com.shivansh.budgettracker

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_add_transaction.amountinput
import kotlinx.android.synthetic.main.activity_add_transaction.amountlayout
import kotlinx.android.synthetic.main.activity_add_transaction.closebutton
import kotlinx.android.synthetic.main.activity_add_transaction.descriptioninput
import kotlinx.android.synthetic.main.activity_add_transaction.labelinput
import kotlinx.android.synthetic.main.activity_add_transaction.labellayout
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class detail : AppCompatActivity() {
    private lateinit var transaction: Transaction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        transaction = intent.getSerializableExtra("transaction")as Transaction

        labelinput.setText(transaction.label)
        amountinput.setText(transaction.amount.toString())
        descriptioninput.setText(transaction.description)


        rootView.setOnClickListener{
            this.window.decorView.clearFocus()


            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }


        labelinput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
            if(it!!.count()>0)
                labellayout.error = null
        }

        amountinput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
            if(it!!.count()>0)
                amountlayout.error = null
        }

        descriptioninput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
        }

        updateButton.setOnClickListener{
            val label : String = labelinput.text.toString()
            val description : String  = descriptioninput.text.toString()
            val amount : Double? = amountinput.text.toString().toDoubleOrNull()

            if(label.isEmpty())
                labellayout.error = "Please enter a valid label"

            else if(amount == null)
                amountlayout.error = " Please enter a valid amount"
            else{
                val transaction = Transaction(transaction.id, label,amount,description )
                update(transaction)
            }
        }
        closebutton.setOnClickListener{
            finish()
        }

    }

    private fun update(transaction: Transaction){
        val  db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().update(transaction)
            finish()
        }
    }
}
