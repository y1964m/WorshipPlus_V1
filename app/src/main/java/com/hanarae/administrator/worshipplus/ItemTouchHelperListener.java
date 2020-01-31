package com.hanarae.administrator.worshipplus;

public interface ItemTouchHelperListener {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemRemove(int position);
}
