package com.rightone.ROChildWater;

public class ProviderDetails {
        public String mBusinessImage;
        public String mBusinessName;
        public String mLocation;
        public String mSpeciality;
        public Boolean mSubscription;
        public String mProviderId;
        public String mRating;
        public String mDescription;
        public String mServiceName;


        public ProviderDetails (String businessImage, String businessName, String location, String specialities, Boolean subscription, String providerId, String rating, String description, String service) {
            this.mBusinessImage = businessImage;
            this.mBusinessName = businessName;
            this.mLocation = location;
            this.mSpeciality = specialities;
            this.mSubscription = subscription;
            this.mProviderId = providerId;
            this.mRating = rating;
            this.mDescription = description;
            this.mServiceName = service;
        }

        public String getBusinessImage(){
            return mBusinessImage;
        }
        public String getBusinessName(){
            return mBusinessName;
        }
        public String getLocation(){
            return mLocation;
        }
        public String getSpeciality(){
            return mSpeciality;
        }
        public Boolean getSubscription(){
            return mSubscription;
        }
        public String getProvider(){
            return mProviderId;
        }
        public String getRating(){
            return mRating;
        }
        public String getDescription(){ return mDescription; }
        public String getServiceName(){ return mServiceName; }
    }
