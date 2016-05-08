package com.conference.ejb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conference.jpa.Article;
import com.conference.jpa.Auteur;
import com.conference.jpa.Chair;
import com.conference.jpa.Personne;
import com.conference.jpa.Programme;
import com.conference.jpa.Session;

/**
 * Session Bean implementation class PersonneBean
 */
@Stateless
@LocalBean
public class ConferenceBean {

    @PersistenceContext
    private EntityManager em;

    // ************************************************************************
    // ********************* Gestion des Personnes*****************************
    // ************************************************************************

    // Apdate or Add Personne Object #### OK ####
    public void addorUpdatePersonne( Personne p ) {
        System.out.println( "addorUpdatePersonne Function" );
        // Personne per = null;
        // per = getPersonneByCin( p );
        // sSystem.out.println( per );
        // System.out.println( per.getCin() );
        if ( getPersonneByCin( p ) == null ) {
            System.out.println( "NULL" );

            em.persist( p );

        } else {
            System.out.println( "NOT NULL" );
            em.merge( p );

        }
    }

    // Delete Personne ### OK 50% ####
    // delete pour admin et auteur **/
    public void deletePersonne( Personne p ) {
        System.out.println( "deletePersonne Function" );
        Personne pr = getPersonneByCin( p );
        if ( pr instanceof Chair ) {
            Chair chair = (Chair) pr;
            for ( Session s : chair.getSessionList() ) {

                Session newSession = getSession( s );
                newSession.setChair( null );
                addorUpdateSession( newSession );
            }
            ( (Chair) pr ).setSessionList( null );
            em.remove( getPersonneByCin( pr ) );
        } else if ( pr instanceof Auteur ) {
            Auteur auteur = (Auteur) pr;
            for ( Article art : auteur.getArticles() ) {
                if ( art.getAuteurs().size() < 2 )
                    deleteArticle( art );
                else if ( art.getAuteurs().size() > 1 ) {
                    auteur.getArticles().remove( art );
                    Article newart = getArticle( art );
                    newart.getAuteurs().remove( auteur );
                    addorUpdateArticle( newart );
                }
            }
            ( (Auteur) pr ).setArticles( null );
            em.remove( getPersonneByCin( pr ) );

        }
    }

    // Get Personne by CIN ### OK ###
    public Personne getPersonneByCin( Personne p ) {
        System.out.println( "getPersonneByCin Function" );
        Personne per = null;
        try {
            per = (Personne) em.createNamedQuery( "Personne.findByCin" )
                    .setParameter( "cin", p.getCin() )
                    .getSingleResult();
        } catch ( Exception e ) {
            e.getMessage();
        }
        return per;
    }

    // Get ALL Personne
    public List<Personne> getAllPersonne() {
        System.out.println( "getAllPersonne Function" );
        List<Personne> personnes = null;
        personnes = em.createNamedQuery( "Personne.findAll" ).getResultList();
        return personnes;
    }

    // ************************************************************************
    // ************************Gestion des Session ****************************
    // ************************************************************************

    // Add or Update Session ### OK ###
    public void addorUpdateSession( Session s ) {
        System.out.println( "addorUpdateSession function" );

        Session ss = getSession( s );
        System.out.println( ss.getArticleList().size() );
        if ( ss == null ) {
            try {
                s.setDateFin( setDateFin( s ) );
            } catch ( ParseException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            em.persist( s );
        } else {
            try {
                s.setDateFin( setDateFin( ss ) );
            } catch ( ParseException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            em.merge( s );
        }

    }

    // Delete Session ### OK ###
    public void deleteSession( Session s ) {
        System.out.println( "deleteSession function" );

        Session se = getSession( s );

        for ( Article art : se.getArticleList() ) {
            System.out.println( "IN LOOP" );
            Article newArt = getArticle( art );
            newArt.setSession( null );
            addorUpdateArticle( newArt );
        }
        se.setArticleList( null );
        em.remove( se );

    }

    public Session getSession( Session s ) {
        System.out.println( "getSession function" );

        return em.find( Session.class, s.getSessionId() );
    }

    public List<Session> getAllSession() {
        List<Session> sessions = null;
        sessions = em.createNamedQuery( "Session.findAll" ).getResultList();
        return sessions;
    }

    // ************************************************************************
    // ************************Gestion des Articles ***************************
    // ************************************************************************

    public void addorUpdateArticle( Article art ) {
        System.out.println( "addorUpdateArticle function" );

        if ( getArticle( art ) == null ) {
            em.persist( art );
        } else
            em.merge( art );
    }

    // Delete Article ## OK ##
    public void deleteArticle( Article art ) {
        System.out.println( "deleteArticle function" );
        Article art1 = getArticle( art );
        if ( art1 != null ) {
            for ( Auteur aut : art1.getAuteurs() ) {
                Auteur newAut = (Auteur) getPersonneByCin( aut );
                newAut.getArticles().remove( art1 );
                addorUpdatePersonne( newAut );
            }
            art1.setAuteurs( null );
            em.remove( getArticle( art ) );
        } else {
            throw new RuntimeException( "Article not found" );
        }

    }

    public Article getArticle( Article art ) {
        System.out.println( "getArticle function" );

        return em.find( Article.class, art.getArticleId() );
    }

    public List<Article> getAllArticle() {
        System.out.println( "getAllArticle function" );

        List<Article> articles = null;
        articles = em.createNamedQuery( "Article.findAll" ).getResultList();
        return articles;
    }

    public void deleteAllArticles() {
        List<Article> articles = getAllArticle();
        for ( Article ar : articles ) {
            deleteArticle( ar );
        }
    }

    // ************************************************************************
    // ************************Gestion des Programmes *************************
    // ************************************************************************

    public void addorUpdateProgramme( Programme prg ) {
        if ( getProgramme( prg ) == null ) {
            em.persist( prg );
        } else
            em.merge( prg );
    }

    public void deleteProgramme( Programme prg ) {
        em.remove( getProgramme( prg ) );
    }

    public Programme getProgramme( Programme prg ) {
        return em.find( Programme.class, prg.getProgrammeId() );
    }

    public List<Programme> getAllProgramme() {
        List<Programme> programmes = null;
        programmes = em.createNamedQuery( "Programme.findAll" ).getResultList();
        return programmes;
    }

    public ConferenceBean() {
        // TODO Auto-generated constructor stub
    }

    private Date setDateFin( Session s ) throws ParseException {
        System.out.println( "setDateFin" );
        Date date = null;
        int f = 0;
        SimpleDateFormat sdf = new SimpleDateFormat( "hh:mm" );
        Calendar cal = Calendar.getInstance();
        cal.setTime( s.getDateDebute() );
        System.out.println( s.getArticleList().size() );
        for ( Article ar : s.getArticleList() ) {
            System.out.println( "duree : " + ar.getDureeArticle() );
            System.out.println( "Article ID : " + ar.getArticleId() );
            f += ar.getDureeArticle();
        }
        cal.add( Calendar.MINUTE, f );
        // System.out.println( f );
        date = sdf.parse( sdf.format( cal.getTime() ) );
        return date;
    }
}
