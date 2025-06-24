package br.com.petzon.petzonapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*; // Atenção ao import: javax.persistence

@Entity
@Table(name = "pets") // Mapeia esta classe para a tabela "pets" que criamos
@Data // Lombok: gera getters, setters, toString, etc.
@NoArgsConstructor // Lombok: gera um construtor sem argumentos, exigido pelo JPA
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Diz ao JPA para salvar o nome do enum ("CACHORRO", "GATO") como String
    @Column(name = "tipo", nullable = false)
    private PetType tipo;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "temperamento", nullable = false)
    private String temperamento;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "idade", nullable = false)
    private int idade;

    @Column(name = "url_foto", nullable = false)
    private String urlFoto;
}
