import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {PropertyType} from '../models/property-type.enum';
import {PropertyService} from '../services/propertyService';
import {HeaderComponent} from '../components/shared/header/header.component';
import {environment} from '../../enviroments/enviroment';
import {ToastService} from '../services/toast.service';

@Component({
  selector: 'app-property-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HeaderComponent],
  templateUrl: './property-form.html',
  styleUrls: ['./property-form.css']
})
export class PropertyFormComponent implements OnInit {
  propertyForm!: FormGroup;
  propertyTypes = Object.values(PropertyType);
  showImageUpload: boolean = false;
  isSubmitting: boolean = false;
  submitSuccess: boolean = false;
  submitError: string = '';
  editMode: boolean = false;
  editPropertyId: number | null = null;

  selectedFiles: File[] = [];
  imagePreviews: string[] = [];
  existingPhotos: { id: number; url: string }[] = [];

  private readonly apiBase = environment.apiUrl;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly propertyService = inject(PropertyService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  ngOnInit(): void {
    this.propertyForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      description: [''],
      price: [null, [Validators.required, Validators.min(0.01)]],
      availableVacancies: [null, [Validators.required, Validators.min(1)]],
      type: [null, [Validators.required]],

      address: this.fb.group({
        street: ['', Validators.required],
        number: ['', Validators.required],
        district: ['', Validators.required],
        city: ['', Validators.required],
        state: ['', Validators.required],
        cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]]
      }),

      acceptAnimals: [false],
      hasGarage: [false],
      gender: ['MIXED']
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editMode = true;
      this.editPropertyId = +id;
      this.loadPropertyData(this.editPropertyId);
    }
  }

  loadPropertyData(id: number): void {
    this.propertyService.getDetailById(id).subscribe({
      next: (property) => {
        this.propertyForm.patchValue({
          title: property.titulo,
          description: property.descricao,
          price: property.preco,
          availableVacancies: property.vagasDisponiveis,
          type: property.tipo,
          acceptAnimals: property.aceitaAnimais,
          hasGarage: property.temGaragem,
          gender: property.generoMoradores,
          address: {
            street: property.rua,
            number: property.numEndereco,
            district: property.bairro,
            city: property.cidade,
            state: property.estado,
            cep: property.cep
          }
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar imóvel:', err);
        this.toast.error('Erro ao carregar dados do imóvel.');
      }
    });

    this.propertyService.getById(id).subscribe({
      next: (property) => {
        if (property.photos && property.photos.length > 0) {
          this.existingPhotos = property.photos.map((p: any) => ({
            id: p.id,
            url: p.path.startsWith('http') ? p.path : this.apiBase + p.path
          }));
          this.showImageUpload = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Erro ao carregar fotos:', err);
      }
    });
  }

  removeExistingPhoto(index: number): void {
    this.existingPhotos.splice(index, 1);
    this.cdr.detectChanges();
  }

  toggleImageUpload(): void {
    this.showImageUpload = !this.showImageUpload;
  }

  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      for (const file of files) {
        this.selectedFiles.push(file);

        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.imagePreviews.push(e.target.result);
          this.cdr.detectChanges();
        };
        reader.readAsDataURL(file);
      }
    }
  }

  removeImage(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.imagePreviews.splice(index, 1);
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.propertyForm.valid) {
      this.isSubmitting = true;

      const formData = new FormData();
      formData.append('data', new Blob([JSON.stringify(this.propertyForm.value)], {
        type: 'application/json'
      }));

      for (const file of this.selectedFiles) {
        formData.append('photos', file);
      }

      if (this.editMode && this.editPropertyId) {
        this.propertyService.updateProperty(this.editPropertyId, formData).subscribe({
          next: () => {
            this.submitSuccess = true;
            this.submitError = '';
            this.isSubmitting = false;
            this.toast.success('Imóvel atualizado com sucesso!');
            setTimeout(() => this.router.navigate(['/meus-imoveis']), 1500);
          },
          error: (err) => {
            console.error('Erro ao atualizar imóvel:', err);
            this.submitError = 'Erro ao atualizar o imóvel. Tente novamente mais tarde.';
            this.submitSuccess = false;
            this.isSubmitting = false;
            this.toast.error(this.submitError);
          }
        });
      } else {
        this.propertyService.createProperty(formData).subscribe({
          next: () => {
            this.submitSuccess = true;
            this.submitError = '';
            this.isSubmitting = false;
            this.toast.success('Imóvel cadastrado com sucesso!');
            setTimeout(() => this.router.navigate(['/meus-imoveis']), 1500);
          },
          error: (err) => {
            console.error('Erro ao salvar imóvel:', err);
            this.submitError = 'Erro ao cadastrar o imóvel. Tente novamente mais tarde.';
            this.submitSuccess = false;
            this.isSubmitting = false;
            this.toast.error(this.submitError);
          }
        });
      }
    } else {
      this.propertyForm.markAllAsTouched();
    }
  }


  getLabelForType(type: string): string {
    switch (type) {
      case PropertyType.HOUSE:
        return 'Casa';
      case PropertyType.APARTMENT:
        return 'Apartamento';
      case PropertyType.STUDIO:
        return 'Studio';
      case PropertyType.ROOM:
        return 'Quarto';
      case PropertyType.DORMITORY:
        return 'República';
      default:
        return type;
    }
  }
}
