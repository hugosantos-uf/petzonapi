package br.com.petzon.petzonapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    @JsonIgnore
    private Usuario responsavel;
}
