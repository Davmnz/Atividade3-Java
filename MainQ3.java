class Deposito {
    private int itens = 0;

    // Adiciona 'qtd' itens (acesso exclusivo)
    public synchronized void colocar(int qtd) {
        itens += qtd;
        System.out.println("[Depósito] +" + qtd + " -> total: " + itens);
    }

    // Tenta retirar 'qtd' itens; retorna true se conseguiu, false se não havia
    public synchronized boolean retirar(int qtd) {
        if (itens >= qtd) {
            itens -= qtd;
            System.out.println("[Depósito] -" + qtd + " -> total: " + itens);
            return true;
        }
        // pré-condição falhou: não há itens suficientes
        return false;
    }

    public synchronized int getItens() { return itens; }
}

// Produz 100 caixas com pausa contolada
class Produtor implements Runnable {
    private final Deposito dep;
    private final int pausaMs; // tempo entre produções

    public Produtor(Deposito d, int pausaMs) {
        this.dep = d; this.pausaMs = pausaMs;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 100; i++) {
            dep.colocar(1); // produz 1 caixa por ciclo
            try { Thread.sleep(pausaMs); } catch (InterruptedException ignored) {}
        }
        System.out.println("[Produtor] Produção encerrada (100 caixas). Total no depósito: " + dep.getItens());
    }
}

// Consome 20 caixas se faltar espera 200 ms e tenta novamente
class Consumidor implements Runnable {
    private final Deposito dep;
    private final int pausaEntreConsumosMs; // pausa quando a retirada deu certo

    public Consumidor(Deposito d, int pausaEntreConsumosMs) {
        this.dep = d; this.pausaEntreConsumosMs = pausaEntreConsumosMs;
    }

    @Override
    public void run() {
        int consumidas = 0;
        while (consumidas < 20) {
            boolean ok = dep.retirar(1);
            if (ok) {
                consumidas++;
                try { Thread.sleep(pausaEntreConsumosMs); } catch (InterruptedException ignored) {}
            } else {
                // pré-condição não satisfeita: aguarda e tenta novamente
                System.out.println("[Consumidor] Sem itens; aguardando 200 ms para tentar novamente...");
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
        System.out.println("[Consumidor] Consumo encerrado (20 caixas). Total no depósito: " + dep.getItens());
    }
}

class MainQ3 {
    public static void main(String[] args) throws Exception {
        Deposito dep = new Deposito();

        int pausaProdutorMs  = 20;  // intervalo entre produções
        int pausaConsumidorMs = 50; // intervalo normal entre consumos bem-sucedidos

        Thread tp = new Thread(new Produtor(dep, pausaProdutorMs));
        Thread tc = new Thread(new Consumidor(dep, pausaConsumidorMs));

        System.out.println(">>> Iniciando produtor e consumidor (retentativa do consumidor: 200 ms)...");
        tp.start();
        tc.start();

        // Aguarda ambos terminarem
        tp.join();
        tc.join();

        System.out.println("[Main] Fim. Total final no depósito: " + dep.getItens());
    }
}
