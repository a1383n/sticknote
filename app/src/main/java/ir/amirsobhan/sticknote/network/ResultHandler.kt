package ir.amirsobhan.sticknote.network

import com.google.gson.Gson
import retrofit2.Response

interface OnCompleteListener {
    fun onSuccess(resultBody: ResultBody?)

    fun onError(resultBody: ResultBody)
}

class ResultHandler{
    companion object{
        fun handleResponse(response: Response<ResultBody>?, onCompleteListener: OnCompleteListener){
            if (response?.isSuccessful == true){
                onCompleteListener.onSuccess(response?.body())
            }else{
                onCompleteListener.onError(Gson().fromJson(response?.errorBody()?.string(),ResultBody::class.java))
            }
        }
    }
}