package com.android.eazymvp.base.baseInterface;


import com.android.eazymvp.base.baseimpl.model.BaseModel;

public interface IBaseModel<T extends BaseModel> {
   T initModel();
}
