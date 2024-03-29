package com.mankirat.approck.lib.activity

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mankirat.approck.lib.R
import com.mankirat.approck.lib.Utils
import com.mankirat.approck.lib.adapter.PlansAdapter
import com.mankirat.approck.lib.databinding.ActivitySubscriptionBinding
import com.mankirat.approck.lib.model.PurchaseModel

class SubsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding
    private var detailData: PurchaseModel.PurchaseDetailModel? = null
    private lateinit var adapter: PlansAdapter

    companion object {
        const val FROM_SPLASH = "from_splash"

        var purchaseModel: PurchaseModel? = null
        var selectedSubs = ""
        var plansVisibility = false
        var callback: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetting.setOnClickListener {
            back()
        }
        binding.tvTerms.movementMethod = ScrollingMovementMethod()

        val productList = purchaseModel?.productDetails
        adapter = PlansAdapter()
        binding.recyclerPlans.adapter = adapter

        if (productList != null) adapter.loadData(productList)

        adapter.clickListener = {
            selectedSubs = productList?.get(it)?.productId.toString()
            if (productList?.get(it)?.freeTrialPeriod != "") {
                binding.txtFreeTrail.visibility = View.VISIBLE
                setFreeTrialData(productList?.get(it))
            } else {
                binding.txtFreeTrail.visibility = View.INVISIBLE
                binding.btnSubscribe.text = resources.getString(R.string.subscribe_now)
            }
        }

        listeners()

        var noFreeTrial = true
        if (productList?.isNotEmpty() == true) {
            for (i in 0 until productList.size) {
                if (productList[i].productId == selectedSubs) {
                    detailData = productList[i]
                    selectedSubs = detailData?.productId.toString()
                    if (productList[i].freeTrialPeriod != "") {
                        noFreeTrial = false
                        binding.txtFreeTrail.visibility = View.VISIBLE
                        setFreeTrialData(productList[i])
                    } else {
                        binding.txtFreeTrail.visibility = View.INVISIBLE
                        binding.btnSubscribe.text = resources.getString(R.string.subscribe_now)
                    }
                }
            }
        }
        if (noFreeTrial && productList?.isNotEmpty() == true) {
            binding.txtFreeTrail.visibility = View.VISIBLE
            setFreeTrialData(detailData)
        }

        binding.recyclerPlans.visibility = if (plansVisibility) View.VISIBLE else View.GONE

        Utils.subsCallback = {
            back()
        }
    }

    override fun onDestroy() {
        Utils.subsCallback = null
        super.onDestroy()
    }

    private fun listeners() {
        binding.btnSubscribe.setOnClickListener {
            Utils.buySubscription?.invoke(selectedSubs)
        }
    }

    private fun setFreeTrialData(detailData: PurchaseModel.PurchaseDetailModel?) {
        val sDuration = when {
            detailData?.subscriptionPeriod?.substring(2) == "M" -> getString(R.string.month)
            detailData?.subscriptionPeriod?.substring(2) == "W" -> getString(R.string.week)
            else -> getString(R.string.year)
        }

        val durationName = if (detailData?.freeTrialPeriod != null && detailData.freeTrialPeriod != "") {
            when {
                detailData.freeTrialPeriod?.substring(2) == "D" -> getString(R.string.day)
                detailData.freeTrialPeriod?.substring(2) == "W" -> getString(R.string.week)
                else -> ""
            }
        } else ""

        val message = if (durationName == "") "${detailData?.originalPrice}/$sDuration"
        else {
            val afterFree = resources.getString(R.string.after_free)
            val trial = resources.getString(R.string.trial)
            "${detailData?.originalPrice}/$sDuration $afterFree ${detailData?.freeTrialPeriod?.substring(1, 2)}-$durationName $trial"
        }

        binding.txtFreeTrail.text = message
    }

    private fun back() {
        if (intent != null && intent.getBooleanExtra(FROM_SPLASH, false)) {
            callback?.invoke()
            finishAffinity()
        } else onBackPressed()
    }
}