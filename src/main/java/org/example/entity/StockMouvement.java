package org.example.entity;


import java.time.LocalDateTime;

public class StockMouvement {
    private int id;
    private MouvementType mouvementType;
    private Unity unity;
    private LocalDateTime mouvementDate;

    public StockMouvement(int id, MouvementType mouvementType, Unity unity, LocalDateTime mouvementDate) {
        this.id = id;
        this.mouvementType = mouvementType;
        this.unity = unity;
        this.mouvementDate = mouvementDate;
    }
}
