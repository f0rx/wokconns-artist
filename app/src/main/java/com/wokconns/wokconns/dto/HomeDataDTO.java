package com.wokconns.wokconns.dto;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeDataDTO implements Serializable {
    ArrayList<HomeBannerDTO> banner = new ArrayList<>();
    ArrayList<HomeNearByJobsDTO> near_by_jobs = new ArrayList<>();
    ArrayList<HomeRecomendedDTO> recomended = new ArrayList<>();
    ArrayList<HistoryDTO> invoice = new ArrayList<>();
    ArrayList<GalleryDTO> gallery = new ArrayList<>();
    ArrayList<ProductDTO> services = new ArrayList<>();

    public ArrayList<HomeBannerDTO> getBanner() {
        return banner;
    }

    public void setBanner(ArrayList<HomeBannerDTO> banner) {
        this.banner = banner;
    }

    public ArrayList<HomeNearByJobsDTO> getNear_by_jobs() {
        return near_by_jobs;
    }

    public void setNear_by_jobs(ArrayList<HomeNearByJobsDTO> near_by_jobs) {
        this.near_by_jobs = near_by_jobs;
    }

    public ArrayList<HomeRecomendedDTO> getRecomended() {
        return recomended;
    }

    public void setRecomended(ArrayList<HomeRecomendedDTO> recomended) {
        this.recomended = recomended;
    }

    public ArrayList<HistoryDTO> getInvoice() {
        return invoice;
    }

    public void setInvoice(ArrayList<HistoryDTO> invoice) {
        this.invoice = invoice;
    }

    public ArrayList<GalleryDTO> getGallery() {
        return gallery;
    }

    public void setGallery(ArrayList<GalleryDTO> gallery) {
        this.gallery = gallery;
    }

    public ArrayList<ProductDTO> getServices() {
        return services;
    }

    public void setServices(ArrayList<ProductDTO> services) {
        this.services = services;
    }
}
