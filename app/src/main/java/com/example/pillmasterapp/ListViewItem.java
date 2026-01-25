package com.example.pillmasterapp;

public class ListViewItem {

    private String pill_name;   // 이름
    private String nickname;    // 별칭
    private String company;     // 제조사
    private String ingredient;  // 성분
    private String volumn;      // 용량
    private String imageUrl;    // 서버에서 받은 이미지 URL

    // Getter & Setter
    public String getPill_name() { return pill_name; }
    public void setPill_name(String pill_name) { this.pill_name = pill_name; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getIngredient() { return ingredient; }
    public void setIngredient(String ingredient) { this.ingredient = ingredient; }

    public String getVolumn() { return volumn; }
    public void setVolumn(String volumn) { this.volumn = volumn; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}


