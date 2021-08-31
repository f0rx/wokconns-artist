package com.wokconns.wokconns.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wokconns.wokconns.R
import com.wokconns.wokconns.databinding.FragmentWalletBinding
import com.wokconns.wokconns.dto.UserDTO
import com.wokconns.wokconns.dto.WalletCurrencyDTO
import com.wokconns.wokconns.dto.WalletHistory
import com.wokconns.wokconns.https.HttpsRequest
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.interfacess.Helper
import com.wokconns.wokconns.network.NetworkManager
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.ui.activity.AddMoney
import com.wokconns.wokconns.ui.activity.BaseActivity
import com.wokconns.wokconns.ui.adapter.AdapterWalletHistory
import com.wokconns.wokconns.utils.ProjectUtils.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Wallet : Fragment(), View.OnClickListener, OnRefreshListener {
    private lateinit var mView: View
    private var adapterWalletHistory: AdapterWalletHistory? = null
    private var walletHistoryList: ArrayList<WalletHistory> = arrayListOf()
    private var walletCurrencyList: ArrayList<WalletCurrencyDTO> = arrayListOf()
    private val TAG = Wallet::class.java.simpleName
    private var mLayoutManager: LinearLayoutManager? = null
    private var prefs: SharedPrefs? = null
    private var userDTO: UserDTO? = null
    private var status = ""
    private var params: HashMap<String, String?> = HashMap<String, String?>()
    private var paramsGetWallet = HashMap<String, String?>()
    private var amt: String? = ""
    private var currency: String? = ""
    private lateinit var baseActivity: BaseActivity
    lateinit var binding: FragmentWalletBinding
    private var walletCurrencyDTO: WalletCurrencyDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false)
        mView = binding.root
        prefs = SharedPrefs.getInstance(activity)
        userDTO = prefs?.getParentUser(Const.USER_DTO)
        baseActivity.headerNameTV.text = resources.getString(R.string.ic_wallet)
        paramsGetWallet[Const.USER_ID] = userDTO?.user_id
        params = HashMap()
        params[Const.USER_ID] = userDTO?.user_id

        // I had to set the default to Naira & add to the list
        val defaultNaira = WalletCurrencyDTO()
        defaultNaira.currency_id = "2"
        defaultNaira.currency_type = "₦"
        defaultNaira.currency_name = "Naira"
        defaultNaira.currency_code = "NGN"
        walletCurrencyList = ArrayList()
        walletCurrencyList.add(defaultNaira)
        walletCurrencyDTO = defaultNaira
//        currencyCode = defaultNaira.currency_code
        /// Fund the Access bank with 500
        /// Get Standard card

        setUiAction()
        return mView
    }

    fun setUiAction() {
        binding.tvAll.setOnClickListener(this)
        binding.tvDebit.setOnClickListener(this)
        binding.tvCredit.setOnClickListener(this)
        binding.llAddMoney.setOnClickListener(this)
        mLayoutManager = LinearLayoutManager(activity?.applicationContext)
        binding.RVhistorylist.layoutManager = mLayoutManager
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.etCurrency.setOnClickListener { v: View? -> binding.etCurrency.showDropDown() }
        binding.etCurrency.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, _: View?, position: Int, _: Long ->
                binding.etCurrency.showDropDown()
                walletCurrencyDTO = parent.getItemAtPosition(position) as WalletCurrencyDTO
                Log.e(TAG, "onItemClick: " + walletCurrencyDTO?.currency_code)
                setWalletData(position)
                filter(walletCurrencyDTO)
            }

        binding.etCurrency.postDelayed({
            binding.etCurrency.setSelection(binding.etCurrency.text.length)
            currency = if (walletCurrencyDTO != null) walletCurrencyDTO?.currency_type else "NGN"
            binding.etCurrency.setText(String.format("%s", walletCurrencyDTO), false)
        }, 500)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llAddMoney -> if (NetworkManager.isConnectToInternet(activity)) {
                val `in` = Intent(activity, AddMoney::class.java)
                `in`.putExtra(Const.AMOUNT, amt)
                `in`.putExtra(Const.CURRENCY, currency)
                startActivity(`in`)
            } else showToast(activity, resources.getString(R.string.internet_concation))
            R.id.tvAll -> {
                setSelected(firstBTN = true, secondBTN = false, thirdBTN = false)
                try {
                    showData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tvCredit -> {
                setSelected(firstBTN = false, secondBTN = false, thirdBTN = true)
                status = "0"
                try {
                    walletCurrencyDTO = walletCurrencyList[0]
                    if (walletCurrencyDTO != null) {
                        updateAccordingStatus(walletCurrencyDTO!!, "0")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tvDebit -> {
                setSelected(firstBTN = false, secondBTN = true, thirdBTN = false)
                status = "1"
                try {
                    walletCurrencyDTO = walletCurrencyList[0]
                    if (walletCurrencyDTO != null) {
                        updateAccordingStatus(walletCurrencyDTO!!, "1")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun walletHistory() {
        showProgressDialog(activity, true, resources.getString(R.string.please_wait))

        HttpsRequest(
            Const.GET_WALLET_HISTORY_NEW_API, params, requireActivity()
        ).stringGet(TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    pauseProgressDialog()

                    binding.swipeRefreshLayout.isRefreshing = false

                    if (flag) {
                        binding.tvNo.visibility = View.GONE
                        binding.RVhistorylist.visibility = View.VISIBLE
                        try {
                            walletCurrencyList = ArrayList()
                            val getPetDTO = object : TypeToken<List<WalletCurrencyDTO?>?>() {}.type

                            walletCurrencyList = Gson().fromJson<Any>(
                                response?.getJSONObject("data")
                                    ?.getJSONArray("currency").toString(), getPetDTO
                            ) as ArrayList<WalletCurrencyDTO>

                            if (walletCurrencyList.size > 0) {
                                val currencyAdapter = ArrayAdapter(
                                    baseActivity, android.R.layout.simple_list_item_1,
                                    walletCurrencyList
                                )
                                binding.etCurrency.setAdapter(currencyAdapter)
                                binding.etCurrency.isCursorVisible = false
                                binding.etCurrency.setText(
                                    binding.etCurrency.adapter.getItem(0).toString(), false
                                )
                                setWalletData(0)
                            }
                            showData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        binding.tvNo.visibility = View.VISIBLE
                        binding.RVhistorylist.visibility = View.GONE
                    }
                }
            }
        )
    }

    private fun setWalletData(index: Int) {
        amt = walletCurrencyList[index].amount
        currency = walletCurrencyList[index].currency_type
        binding.tvWallet.text = String.format("%s %s", currency, amt)
        walletHistoryList = walletCurrencyList[index].wallet_history
    }

    override fun onResume() {
        super.onResume()
        //        getWallet();
        binding.swipeRefreshLayout.post {
            Log.e("Runnable", "FIRST")
            if (NetworkManager.isConnectToInternet(activity)) {
                binding.swipeRefreshLayout.isRefreshing = true
                walletHistory()
            } else {
                showToast(activity, resources.getString(R.string.internet_concation))
            }
        }
    }

    private fun wallet() {
        HttpsRequest(
            Const.GET_WALLET_API, paramsGetWallet, requireActivity(),
        ).stringPost(TAG, object : Helper {
            override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                pauseProgressDialog()
                if (flag) {
                    try {
                        amt = response?.getJSONObject("data")?.optString("amount")
                        currency = response?.getJSONObject("data")?.optString("currency_type")
                        binding.tvWallet.text = String.format("%s %s", currency, amt)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    fun showData() {
        if (walletHistoryList.size > 0) {
            binding.tvNo.visibility = View.GONE
            binding.RVhistorylist.visibility = View.VISIBLE
            adapterWalletHistory = AdapterWalletHistory(this@Wallet, walletHistoryList)
            binding.RVhistorylist.adapter = adapterWalletHistory
        } else {
            binding.tvNo.visibility = View.VISIBLE
            binding.RVhistorylist.visibility = View.GONE
        }
    }

    override fun onRefresh() {
        Log.e("ONREFREST_Firls", "FIRS")
        walletHistory()
    }

    fun setSelected(firstBTN: Boolean, secondBTN: Boolean, thirdBTN: Boolean) {
        if (firstBTN) {
            binding.tvAllSelect.visibility = View.VISIBLE
            binding.tvDebitSelect.visibility = View.GONE
            binding.tvCreditSelect.visibility = View.GONE
        }
        if (secondBTN) {
            binding.tvDebitSelect.visibility = View.VISIBLE
            binding.tvAllSelect.visibility = View.GONE
            binding.tvCreditSelect.visibility = View.GONE
        }
        if (thirdBTN) {
            binding.tvCreditSelect.visibility = View.VISIBLE
            binding.tvAllSelect.visibility = View.GONE
            binding.tvDebitSelect.visibility = View.GONE
        }
        binding.tvAllSelect.isSelected = firstBTN
        binding.tvDebitSelect.isSelected = secondBTN
        binding.tvCreditSelect.isSelected = secondBTN
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
    }

    private fun filter(dto: WalletCurrencyDTO?) {
        val filterdNames = ArrayList(dto!!.wallet_history)
        adapterWalletHistory?.updateList(filterdNames)
    }

    private fun updateAccordingStatus(dto: WalletCurrencyDTO, status: String) {
        val walletHistoryArrayList = ArrayList<WalletHistory>()
        for (dto1 in dto.wallet_history) {
            if (dto1.status.equals(status, ignoreCase = true)) {
                walletHistoryArrayList.add(dto1)
            }
        }
        adapterWalletHistory?.updateList(walletHistoryArrayList)
    }
}