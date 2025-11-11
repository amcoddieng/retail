package com.example.retail.web.bean;

import com.example.retail.domain.Livraison;
import com.example.retail.domain.StatutCommande;
import com.example.retail.service.CommandeService;
import com.example.retail.service.LivraisonService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

// PrimeFaces 8+ (Chart.js)
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;

@Named("dashboardBean")
@RequestScoped
public class DashboardBean implements Serializable {

    @EJB
    private LivraisonService livraisonService;

    @EJB
    private CommandeService commandeService;

    private LineChartModel ventesModel;
    private List<Livraison> livraisonsRecentes;

    // KPIs
    private int ventesJour;
    private int nbEnAttente;
    private int nbRuptures;
    private double delaiMoyen;

    @PostConstruct
    public void init() {
        System.out.println(">>> DashboardBean initialisé");
        chargerKPIs();
        chargerLivraisonsRecentes();
        construireGraphiqueVentes();
    }

    private void chargerKPIs() {
        try {
            // Utilisation des services métier
            ventesJour = commandeService.compterVentesDuJour();
            nbEnAttente = commandeService.listByStatut(StatutCommande.EN_ATTENTE_PAIEMENT).size();
            nbRuptures = livraisonService.compterRupturesStock();
            delaiMoyen = livraisonService.calculerDelaiMoyenLivraisonV2();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des KPIs: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            ventesJour = 0;
            nbEnAttente = 0;
            nbRuptures = 0;
            delaiMoyen = 0.0;
        }
    }

    private void chargerLivraisonsRecentes() {
        try {
            livraisonsRecentes = livraisonService.dernieres(6);
            if (livraisonsRecentes == null) {
                livraisonsRecentes = Collections.emptyList();
            }
        } catch (Exception e) {
            livraisonsRecentes = Collections.emptyList();
        }
    }

    private void construireGraphiqueVentes() {
        try {
            ventesModel = new LineChartModel();
            ChartData data = new ChartData();

            LineChartDataSet dataSet = new LineChartDataSet();
            dataSet.setLabel("Ventes des 7 derniers jours");
            dataSet.setFill(false);
            dataSet.setLineTension(0.1);
            dataSet.setBackgroundColor("rgba(75, 192, 192, 0.2)");
            dataSet.setBorderColor("rgba(75, 192, 192, 1)");
            dataSet.setPointBackgroundColor("rgba(75, 192, 192, 1)");

            // Récupérer les données réelles via le service
            List<Number> donneesVentes = obtenirDonneesVentes7Jours();
            dataSet.setData(Collections.singletonList(donneesVentes));

            // Labels des jours
            List<String> labels = genererLabels7Jours();

            data.setLabels(labels);
            data.addChartDataSet(dataSet);
            ventesModel.setData(data);

            // Options du graphique
            ventesModel.setExtender("chartExtender");

        } catch (Exception e) {
            System.err.println("Erreur construction graphique: " + e.getMessage());
            // Graphique de démonstration en cas d'erreur
            ventesModel = buildDemoLineModel();
        }
    }

    private List<Number> obtenirDonneesVentes7Jours() {
        try {
            List<Object[]> resultats = commandeService.getVentes7DerniersJours();
            List<Number> donnees = new java.util.ArrayList<>();

            // Générer les 7 derniers jours
            Calendar cal = Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_YEAR, -i);
                Date dateCourante = cal.getTime();

                // Trouver le count pour cette date
                long count = resultats.stream()
                        .filter(r -> isSameDay((Date)r[0], dateCourante))
                        .mapToLong(r -> (Long)r[1])
                        .findFirst()
                        .orElse(0L);

                donnees.add((int)count);
            }
            return donnees;
        } catch (Exception e) {
            System.err.println("Erreur récupération données ventes: " + e.getMessage());
            // Données de démonstration
            return java.util.Arrays.asList(5, 9, 7, 12, 8, 11, 10);
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private List<String> genererLabels7Jours() {
        String[] jours = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        Calendar cal = Calendar.getInstance();
        List<String> labels = new java.util.ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            int jourSemaine = cal.get(Calendar.DAY_OF_WEEK);
            labels.add(jours[jourSemaine - 1]);
        }

        return labels;
    }

    private LineChartModel buildDemoLineModel() {
        LineChartModel model = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet ds = new LineChartDataSet();
        ds.setLabel("Ventes (démo)");
        ds.setFill(false);
        ds.setData(java.util.Arrays.asList(5, 9, 7, 12, 8, 11, 10));

        data.setLabels(java.util.Arrays.asList("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"));
        data.addChartDataSet(ds);
        model.setData(data);
        return model;
    }

    // Méthode pour rafraîchir manuellement le dashboard
    public void rafraichir() {
        System.out.println(">>> Rafraîchissement manuel du dashboard");
        chargerKPIs();
        chargerLivraisonsRecentes();
        construireGraphiqueVentes();
    }

    // ----- Getters -----
    public int getVentesJour() { return ventesJour; }
    public int getNbEnAttente() { return nbEnAttente; }
    public int getNbRuptures() { return nbRuptures; }
    public double getDelaiMoyen() { return delaiMoyen; }
    public List<Livraison> getLivraisonsRecentes() { return livraisonsRecentes; }
    public LineChartModel getVentesModel() { return ventesModel; }
}