import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { TransactionResponse } from '../../core/models/transaction.model';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe],
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  @Input() isOpen: boolean = false;
  @Input() transaction: TransactionResponse | null = null;
  @Input() deleting: boolean = false;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    if (!this.deleting) {
      this.confirm.emit();
    }
  }

  onCancel(): void {
    if (!this.deleting) {
      this.cancel.emit();
    }
  }
}
