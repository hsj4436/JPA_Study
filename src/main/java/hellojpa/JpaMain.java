package hellojpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        // persistence.xml에 persistence-unit의 name을 hello로 지정해뒀음
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        //code
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 영속
            Member findMember = em.find(Member.class, 4L);
            findMember.setName("B");

            // 준영속
            em.detach(findMember);

            System.out.println("=====================");

            // 실제 insert query 나가는 시점
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
