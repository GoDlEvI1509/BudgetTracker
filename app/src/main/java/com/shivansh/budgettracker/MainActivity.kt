package com.shivansh.budgettracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions : List<Transaction>
    private lateinit var oldtransactions : List<Transaction>
    private lateinit var transactionadapter: transactionadapter
    private lateinit var linearlayoutmanager: LinearLayoutManager
    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactions = arrayListOf()
        transactionadapter = transactionadapter(transactions)
        linearlayoutmanager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this,
        AppDatabase::class.java,
            "transactions").build()

        recycler_view.apply {
            adapter = transactionadapter
            layoutManager = linearlayoutmanager
        }


        // swipe to receive
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }



        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recycler_view)



        addbtn.setOnClickListener{
            val intent = Intent(this, AddTransactionActivity::class.java)

            startActivity(intent)
        }




    }
    private fun fetchAll(){
        GlobalScope.launch {

            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updatedashboard()
                transactionadapter.setData(transactions)
            }
        }

    }
    private fun updatedashboard(){
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmount = transactions.filter { it.amount>0 }.map { it.amount}.sum()
        val expenseAmount = totalAmount - budgetAmount

        balance.text = "Rs. %.2f".format(totalAmount)
        budget.text = "Rs. %.2f".format(budgetAmount)
        expense.text = "Rs. %.2f".format(expenseAmount)
    }

    private fun undoDelete(){
        GlobalScope.launch{
         db.transactionDao().insertAll(deletedTransaction)

            transactions = oldtransactions

            runOnUiThread {
                transactionadapter.setData(transactions)
                updatedashboard()
            }
        }
    }

    private fun showSnackbar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setTextColor(ContextCompat.getColor(this,R.color.white))
            .show()


    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldtransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                updatedashboard()
                transactionadapter.setData(transactions)
                showSnackbar()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}