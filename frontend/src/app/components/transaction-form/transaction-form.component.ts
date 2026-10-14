import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransactionRequest, TransactionResponse, TransactionType } from '../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transaction-form.component.html',
  styleUrls: ['./transaction-form.component.css']
})
export class TransactionFormComponent implements OnInit, OnChanges {
  @Input() isOpen: boolean = false;
  @Input() transactionToEdit: TransactionResponse | null = null;
  @Input() saving: boolean = false;

  @Output() save = new EventEmitter<TransactionRequest>();
  @Output() close = new EventEmitter<void>();

  form!: FormGroup;

  categories: string[] = [
    'Salary',
    'Freelance',
    'Investment',
    'Food',
    'Housing',
    'Utilities',
    'Transportation',
    'Entertainment',
    'Health',
    'Shopping',
    'Education',
    'Miscellaneous'
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['transactionToEdit'] || changes['isOpen']) {
      if (this.isOpen) {
        this.populateForm();
      }
    }
  }

  private initForm(): void {
    const today = new Date().toISOString().split('T')[0];

    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(150)]],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      category: ['', [Validators.required, Validators.maxLength(60)]],
      type: ['EXPENSE', [Validators.required]],
      transactionDate: [today, [Validators.required]],
      notes: ['', [Validators.maxLength(1000)]]
    });
  }

  private populateForm(): void {
    if (!this.form) {
      this.initForm();
    }

    if (this.transactionToEdit) {
      this.form.patchValue({
        title: this.transactionToEdit.title,
        amount: this.transactionToEdit.amount,
        category: this.transactionToEdit.category,
        type: this.transactionToEdit.type,
        transactionDate: this.transactionToEdit.transactionDate,
        notes: this.transactionToEdit.notes || ''
      });
    } else {
      const today = new Date().toISOString().split('T')[0];
      this.form.reset({
        title: '',
        amount: null,
        category: '',
        type: 'EXPENSE',
        transactionDate: today,
        notes: ''
      });
    }
    this.form.markAsPristine();
    this.form.markAsUntouched();
  }

  setType(type: TransactionType): void {
    this.form.get('type')?.setValue(type);
    this.form.get('type')?.markAsDirty();
  }

  onSubmit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.value;
    const request: TransactionRequest = {
      title: val.title.trim(),
      amount: Number(val.amount),
      category: val.category.trim(),
      type: val.type,
      transactionDate: val.transactionDate,
      notes: val.notes ? val.notes.trim() : null
    };

    this.save.emit(request);
  }

  onCancel(): void {
    if (!this.saving) {
      this.close.emit();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'This field is required';
    if (field.errors['min']) return 'Amount must be at least ₹0.01';
    if (field.errors['maxlength']) {
      return `Maximum length is ${field.errors['maxlength'].requiredLength} characters`;
    }
    return 'Invalid input';
  }
}
