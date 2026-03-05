import {Component, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ToastContainerComponent} from './components/shared/toast/toast-container.component';
import {ChatWidgetComponent} from './components/chat-widget/chat-widget.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainerComponent, ChatWidgetComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
