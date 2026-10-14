import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TransactionFormComponent } from './transaction-form.component';
import { SimpleChange } from '@angular/core';

describe('TransactionFormComponent', () => {
  let component: TransactionFormComponent;
  let fixture: ComponentFixture<TransactionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionFormComponent, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the form component', () => {
    expect(component).toBeTruthy();
    expect(component.form).toBeDefined();
  });

  it('should initialize with invalid status due to required fields', () => {
    expect(component.form.valid).toBeFalse();
    expect(component.form.get('title')?.valid).toBeFalse();
    expect(component.form.get('amount')?.valid).toBeFalse();
    expect(component.form.get('category')?.valid).toBeFalse();
  });

  it('should invalidate amount less than 0.01', () => {
    const amountCtrl = component.form.get('amount');
    amountCtrl?.setValue(0);
    expect(amountCtrl?.valid).toBeFalse();

    amountCtrl?.setValue(-50);
    expect(amountCtrl?.valid).toBeFalse();

    amountCtrl?.setValue(100);
    expect(amountCtrl?.valid).toBeTrue();
  });

  it('should emit save event with formatted payload when valid', () => {
    spyOn(component.save, 'emit');

    component.form.setValue({
      title: '  Freelance Web Design  ',
      amount: 25000,
      category: 'Freelance',
      type: 'INCOME',
      transactionDate: '2026-03-10',
      notes: ' Client work '
    });

    expect(component.form.valid).toBeTrue();

    component.onSubmit();

    expect(component.save.emit).toHaveBeenCalledWith({
      title: 'Freelance Web Design',
      amount: 25000,
      category: 'Freelance',
      type: 'INCOME',
      transactionDate: '2026-03-10',
      notes: 'Client work'
    });
  });

  it('should populate form when transactionToEdit is provided', () => {
    component.transactionToEdit = {
      id: 5,
      title: 'Electricity Bill',
      amount: 2400,
      category: 'Utilities',
      type: 'EXPENSE',
      transactionDate: '2026-03-05',
      notes: 'March bill',
      createdAt: '2026-03-05T09:00:00'
    };
    component.isOpen = true;

    component.ngOnChanges({
      transactionToEdit: new SimpleChange(null, component.transactionToEdit, true),
      isOpen: new SimpleChange(false, true, true)
    });

    expect(component.form.get('title')?.value).toBe('Electricity Bill');
    expect(component.form.get('amount')?.value).toBe(2400);
    expect(component.form.get('type')?.value).toBe('EXPENSE');
  });
});
